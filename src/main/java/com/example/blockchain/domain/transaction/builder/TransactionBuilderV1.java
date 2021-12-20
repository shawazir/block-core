package com.example.blockchain.domain.transaction.builder;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.blockchain.domain.address.Address;
import com.example.blockchain.domain.transaction.Transaction;
import com.example.blockchain.domain.transaction.TransactionInput;
import com.example.blockchain.domain.transaction.TransactionOutput;
import com.example.blockchain.domain.transaction.builder.validator.BuildTransactionFormValidationResult;
import com.example.blockchain.domain.transaction.builder.validator.InvalidBuildTransactionFormException;
import com.example.blockchain.domain.transaction.builder.validator.TransactionBuilderV1Validator;
import com.example.blockchain.domain.transaction.script.PayToPublicKeyHashScriptsBuilder;
import com.example.blockchain.domain.transaction.script.PayToPublicKeyScriptsBuilder;
import com.example.blockchain.domain.transaction.script.TransactionScriptsType;
import com.example.blockchain.util.ByteUtil;
import com.example.blockchain.util.HexStringUtil;
import com.example.blockchain.util.PublicPrivateKeysUtil;
import com.example.blockchain.util.ScriptCodesUtil;

public class TransactionBuilderV1 implements TransactionBuilder {

	private static final Logger log = LoggerFactory.getLogger(TransactionBuilderV1.class);

	static final short VERSION = 1;
	static final int MAX_OUTPUT_INDEX = new BigInteger("ff", 16).intValue();
	static final long DEFAULT_SEQUENCE = new BigInteger("ffffffff", 16).longValue();
	static final long DEFAULT_LOCKTIME = 0L;

	private static final int VERSION_SIZE = 1; // In bytes
	private static final int INPUT_COUNT_SIZE = 1; // In bytes
	private static final int TRANSACTION_ID_SIZE = 32; // In bytes
	private static final int OUTPUT_INDEX_SIZE = 1; // In bytes
	private static final int SEQUENCE_SIZE = 4; // In bytes
	private static final int OUTPUT_COUNT_SIZE = 1; // In bytes
	private static final int AMOUNT_SIZE = 8; // In bytes
	private static final int LOCKTIME_SIZE = 4; // In bytes

	private TransactionBuilderV1Validator transactionBuilderV1Validator = new TransactionBuilderV1Validator();

	private PayToPublicKeyScriptsBuilder payToPublicKeyScriptsBuilder;

	private PayToPublicKeyHashScriptsBuilder payToPublicKeyHashScriptsBuilder;

	private Transaction voidTransaction;

	public TransactionBuilderV1(PayToPublicKeyScriptsBuilder payToPublicKeyScriptsBuilder, PayToPublicKeyHashScriptsBuilder payToPublicKeyHashScriptsBuilder,
			Transaction voidTransaction) {
		this.payToPublicKeyScriptsBuilder = payToPublicKeyScriptsBuilder;
		this.payToPublicKeyHashScriptsBuilder = payToPublicKeyHashScriptsBuilder;
		this.voidTransaction = voidTransaction;
	}

	@Override
	public Transaction buildCoinbaseTransaction(BuildCoinbaseTransactionForm form) {
		// Validates the form
		BuildTransactionFormValidationResult validationResult = transactionBuilderV1Validator.validateBuildCoinbaseTransactionForm(form);
		if (validationResult != BuildTransactionFormValidationResult.SUCCESS) {
			throw new InvalidBuildTransactionFormException(validationResult);
		}

		// Builds the transaction
		List<TransactionInput> txInputs = new ArrayList<>(1);
		byte[] scriptSig = buildScriptSigOfCoinbaseTransaction(form.getBlockHeight(), form.getScriptSigText());
		TransactionInput txInput = new TransactionInput(voidTransaction, MAX_OUTPUT_INDEX, scriptSig, DEFAULT_SEQUENCE);
		txInputs.add(txInput);
		List<TransactionOutput> txOutputs = new ArrayList<>(1);
		byte[] scriptPubKey = buildScriptPubKey(form.getScriptsType(), form.getLockingPublicKey(), form.getLockingAddress());
		TransactionOutput txOutput = new TransactionOutput(form.getAmount(), scriptPubKey);
		txOutputs.add(txOutput);
		Transaction tx = new Transaction(VERSION, txInputs, txOutputs, DEFAULT_LOCKTIME);
		return tx;
	}

	private byte[] buildScriptSigOfCoinbaseTransaction(int blockHeight, String scriptSigText) {
		byte[] pushCodeAndValueOfBlockHeight = ScriptCodesUtil.getPushCodeForNumericalValue(blockHeight, true);
		if (scriptSigText != null && scriptSigText.length() > 0) {
			return org.bouncycastle.util.Arrays.concatenate(pushCodeAndValueOfBlockHeight, scriptSigText.getBytes());
		} else {
			return pushCodeAndValueOfBlockHeight;
		}
	}

	@Override
	public Transaction buildTransaction(BuildTransactionForm form) {
		// Validates the form
		BuildTransactionFormValidationResult validationResult = transactionBuilderV1Validator.validateBuildTransactionForm(form);
		if (validationResult != BuildTransactionFormValidationResult.SUCCESS) {
			log.error("Error while building transaction {}" + validationResult);
			throw new InvalidBuildTransactionFormException(validationResult);
		}

		// Builds the transaction
		byte[][] tempScriptSigs = new byte[form.getInputTxs().length][];
		List<TransactionInput> txInputs = new ArrayList<>();
		List<TransactionOutput> txOutputs = new ArrayList<>();
		for (int i = 0; i < form.getInputTxs().length; i++) {
			Transaction tx = form.getInputTxs()[i];
			int indexOfOutput = form.getOutputIndices()[i];
			TransactionInput txInput = new TransactionInput(tx, indexOfOutput, new byte[] {}, DEFAULT_SEQUENCE);
			txInputs.add(txInput);
			tempScriptSigs[i] = form.getInputTxs()[i].getOutputs().get(indexOfOutput).getScriptPubKey();
		}
		for (int i = 0; i < form.getAmounts().length; i++) {
			BigInteger amount = form.getAmounts()[i];
			byte[] scriptPubKey = buildScriptPubKey(form.getScriptsType(), form.getLockingPublicKeys(), form.getLockingAddresses(), i);
			TransactionOutput txOutput = new TransactionOutput(amount, scriptPubKey);
			txOutputs.add(txOutput);
		}
		Transaction tx = new Transaction(null, VERSION, txInputs, txOutputs, form.getLocktime());
		setScriptSigOfTransactions(tx, tempScriptSigs, form.getScriptsType(), form.getUnlockingKeyPairs());
		tx.setId();
		return tx;
	}

	private void setScriptSigOfTransactions(Transaction tx, byte[][] tempScriptSigs, TransactionScriptsType scriptsType, KeyPair[] keyPairs) {
		int numberOfInputs = tx.getInputs().size();
		byte[][] signatures = new byte[numberOfInputs][];
		for (int i = 0; i < numberOfInputs; i++) {
			tx.getInputs().get(i).setScriptSig(tempScriptSigs[i]);
			byte[] tempTxData = serializeTransaction(tx).getData();
			signatures[i] = PublicPrivateKeysUtil.signData(tempTxData, keyPairs[i].getPrivate());
			tx.getInputs().get(i).setScriptSig(new byte[] {});
		}
		for (int i = 0; i < signatures.length; i++) {
			byte[] scriptSig;
			if (scriptsType == TransactionScriptsType.PAY_TO_PUBLIC_KEY) {
				scriptSig = payToPublicKeyScriptsBuilder.buildScriptSig(signatures[i]);
			} else if (scriptsType == TransactionScriptsType.PAY_TO_PUBLIC_KEY_HASH) {
				scriptSig = payToPublicKeyHashScriptsBuilder.buildScriptSig(signatures[i], keyPairs[i].getPublic());
			} else {
				throw new RuntimeException("Unsupported Transaction Scripts Type");
			}
			tx.getInputs().get(i).setScriptSig(scriptSig);
		}
	}

	private byte[] buildScriptPubKey(TransactionScriptsType scriptsType, PublicKey[] lockingPublicKeys, Address[] lockingAddresses, int indexOfLockingData) {
		PublicKey lockingPublicKey = null;
		Address lockingAddress = null;
		if (lockingPublicKeys != null) {
			lockingPublicKey = lockingPublicKeys[indexOfLockingData];
		}
		if (lockingAddresses != null) {
			lockingAddress = lockingAddresses[indexOfLockingData];
		}
		return buildScriptPubKey(scriptsType, lockingPublicKey, lockingAddress);
	}

	private byte[] buildScriptPubKey(TransactionScriptsType scriptsType, PublicKey lockingPublicKey, Address lockingAddress) {
		if (scriptsType == TransactionScriptsType.PAY_TO_PUBLIC_KEY) {
			return payToPublicKeyScriptsBuilder.buildScriptPubKey(lockingPublicKey);
		} else if (scriptsType == TransactionScriptsType.PAY_TO_PUBLIC_KEY_HASH) {
			return payToPublicKeyHashScriptsBuilder.buildScriptPubKey(lockingAddress);
		} else {
			throw new RuntimeException("Unsupported Transaction Scripts Type");
		}
	}

	@Override
	public boolean validateTransaction(Transaction transaction) {
		// TODO Implement this
		return false;
	}

	@Override
	public int calculateTransactionSize(Transaction transaction) {
		int size = 0;
		size += VERSION_SIZE;
		int inputsCount = transaction.getInputs().size();
		byte[] inputsCountByteArray = HexStringUtil.buildVarInt(inputsCount);
		size += inputsCountByteArray.length;
		for (TransactionInput transactionInput : transaction.getInputs()) {
			size += transactionInput.getTransaction().getId().length;
			size += OUTPUT_INDEX_SIZE;
			int scriptSigSize = transactionInput.getScriptSig().length;
			byte[] scriptSigSizeByteArray = HexStringUtil.buildVarInt(scriptSigSize);
			size += scriptSigSizeByteArray.length;
			size += transactionInput.getScriptSig().length;
			size += SEQUENCE_SIZE;
		}
		int outputsCount = transaction.getOutputs().size();
		byte[] outputsCountByteArray = HexStringUtil.buildVarInt(outputsCount);
		size += outputsCountByteArray.length;
		for (TransactionOutput transactionOutput : transaction.getOutputs()) {
			size += AMOUNT_SIZE;
			int scriptPubKeySize = transactionOutput.getScriptPubKey().length;
			byte[] scriptPubKeySizeByteArray = HexStringUtil.buildVarInt(scriptPubKeySize);
			size += scriptPubKeySizeByteArray.length;
			size += transactionOutput.getScriptPubKey().length;
		}
		size += LOCKTIME_SIZE;
		return size;
	}

	/**
	 * Serializes the given transaction.
	 * 
	 */
	@Override
	public TransactionSerializeResult serializeTransaction(Transaction transaction) {
		int transactionSize = calculateTransactionSize(transaction);
		byte[] data = new byte[transactionSize];
		int nextIndex = 0;

		// Version
		byte[] bytes = ByteUtil.convertToByteArray(transaction.getVersion(), VERSION_SIZE);
		System.arraycopy(bytes, 0, data, nextIndex, VERSION_SIZE);
		nextIndex += VERSION_SIZE;

		// Inputs
		TransactionSerializeResult inputsResult = serializeTransactionInputs(transaction.getInputs(), data, nextIndex);
		nextIndex = inputsResult.getNextIndex();

		// Outputs
		TransactionSerializeResult outputsResult = serializeTransactionOutputs(transaction.getOutputs(), data, nextIndex);
		nextIndex = outputsResult.getNextIndex();

		// Locktime
		bytes = ByteUtil.convertToByteArray(transaction.getLocktime(), LOCKTIME_SIZE);
		System.arraycopy(bytes, 0, data, nextIndex, LOCKTIME_SIZE);
		nextIndex += LOCKTIME_SIZE;

		return new TransactionSerializeResult(data, nextIndex);
	}

	/**
	 * Serializes the given input transactions. The output is copied to the given
	 * byte array.
	 * 
	 */
	private TransactionSerializeResult serializeTransactionInputs(List<TransactionInput> transactionInputs, byte[] data, int nextIndex) {
		// Count
		byte[] bytes = ByteUtil.convertToByteArray(transactionInputs.size(), INPUT_COUNT_SIZE);
		System.arraycopy(bytes, 0, data, nextIndex, INPUT_COUNT_SIZE);
		nextIndex += INPUT_COUNT_SIZE;

		for (TransactionInput transactionInput : transactionInputs) {
			// Transaction ID
			System.arraycopy(transactionInput.getTransaction().getId(), 0, data, nextIndex, TRANSACTION_ID_SIZE);
			nextIndex += TRANSACTION_ID_SIZE;

			// Output Index
			bytes = ByteUtil.convertToByteArray(transactionInput.getOutputIndex(), OUTPUT_INDEX_SIZE);
			System.arraycopy(bytes, 0, data, nextIndex, OUTPUT_INDEX_SIZE);
			nextIndex += OUTPUT_INDEX_SIZE;

			// ScriptSig Size
			int scriptSigSize = transactionInput.getScriptSig().length;
			byte[] scriptSigSizeByteArray = HexStringUtil.buildVarInt(scriptSigSize);
			System.arraycopy(scriptSigSizeByteArray, 0, data, nextIndex, scriptSigSizeByteArray.length);
			nextIndex += scriptSigSizeByteArray.length;

			// ScriptSig
			System.arraycopy(transactionInput.getScriptSig(), 0, data, nextIndex, scriptSigSize);
			nextIndex += scriptSigSize;

			// Sequence
			bytes = ByteUtil.convertToByteArray(transactionInput.getSequence(), SEQUENCE_SIZE);
			System.arraycopy(bytes, 0, data, nextIndex, SEQUENCE_SIZE);
			nextIndex += SEQUENCE_SIZE;
		}

		return new TransactionSerializeResult(data, nextIndex);
	}

	/**
	 * Serializes the given output transactions. The output is appended to the given
	 * byte array.
	 * 
	 */
	private TransactionSerializeResult serializeTransactionOutputs(List<TransactionOutput> transactionOutputs, byte[] data, int nextIndex) {
		// Count
		byte[] bytes = ByteUtil.convertToByteArray(transactionOutputs.size(), OUTPUT_COUNT_SIZE);
		System.arraycopy(bytes, 0, data, nextIndex, OUTPUT_COUNT_SIZE);
		nextIndex += OUTPUT_COUNT_SIZE;

		for (TransactionOutput transactionOutput : transactionOutputs) {
			// Amount
			bytes = ByteUtil.convertToByteArray(transactionOutput.getAmount(), AMOUNT_SIZE);
			System.arraycopy(bytes, 0, data, nextIndex, AMOUNT_SIZE);
			nextIndex += AMOUNT_SIZE;

			// ScriptPubKey Size
			int scriptPubKeySize = transactionOutput.getScriptPubKey().length;
			byte[] scriptPubKeySizeByteArray = HexStringUtil.buildVarInt(scriptPubKeySize);
			System.arraycopy(scriptPubKeySizeByteArray, 0, data, nextIndex, scriptPubKeySizeByteArray.length);
			nextIndex += scriptPubKeySizeByteArray.length;

			// ScriptPubKey
			System.arraycopy(transactionOutput.getScriptPubKey(), 0, data, nextIndex, scriptPubKeySize);
			nextIndex += scriptPubKeySize;
		}

		return new TransactionSerializeResult(data, nextIndex);
	}

	/**
	 * Serializes the given transactions.
	 * 
	 */
	@Override
	public byte[] serializeMultipleTransactions(List<Transaction> transactions) {
		// Calculates the count VarInt
		byte[] transactionsCountVarInt = HexStringUtil.buildVarInt(transactions.size());

		// Calculates the total transactions size
		int transactionsSize = 0;
		for (Transaction transaction : transactions) {
			transactionsSize += calculateTransactionSize(transaction);
		}

		// Instantiates the data byte array. Its size equals: transactions size +
		// transactions count VarInt size.
		byte[] data = new byte[transactionsCountVarInt.length + transactionsSize];

		// Appends the transactions count VarInt
		int nextIndex = 0;
		System.arraycopy(transactionsCountVarInt, 0, data, nextIndex, transactionsCountVarInt.length);
		nextIndex += transactionsCountVarInt.length;

		// Appends the transactions data
		for (Transaction tx : transactions) {
			byte[] txData = tx.serialize();
			System.arraycopy(txData, 0, data, nextIndex, txData.length);
			nextIndex += txData.length;
		}

		// Returns
		return data;
	}

	/**
	 * Converts the given transaction String data to its object form.
	 * 
	 */
	@Override
	public Transaction deserializeTransaction(byte[] transactionData) {
		return deserializeTransaction(transactionData, 0).getTransaction();
	}

	/**
	 * Converts the given transaction data to its object form starting with the
	 * given index.
	 * 
	 */
	@Override
	public TransactionBuildResult deserializeTransaction(byte[] data, int dataStartIndex) {
		int nextIndex = dataStartIndex;
		short version = ByteUtil.getShort(data, nextIndex, VERSION_SIZE);
		nextIndex += VERSION_SIZE;
		int inputCount = ByteUtil.getInt(data, nextIndex, INPUT_COUNT_SIZE);
		nextIndex += INPUT_COUNT_SIZE;
		TransactionInputBuildResult transactionInputBuildResult = deserializeTransactionInputs(data, inputCount, nextIndex);
		List<TransactionInput> transactionInputs = transactionInputBuildResult.getTransactionInputs();
		nextIndex = transactionInputBuildResult.getNextIndex();
		int outputCount = ByteUtil.getInt(data, nextIndex, OUTPUT_COUNT_SIZE);
		nextIndex += OUTPUT_COUNT_SIZE;
		TransactionOutputBuildResult transactionOutputBuildResult = deserializeTransactionOutputs(data, outputCount, nextIndex);
		List<TransactionOutput> transactionOutputs = transactionOutputBuildResult.getTransactionOutputs();
		nextIndex = transactionOutputBuildResult.getNextIndex();
		long locktime = ByteUtil.getLong(data, nextIndex, LOCKTIME_SIZE);
		nextIndex += LOCKTIME_SIZE;

		Transaction transaction = new Transaction(version, transactionInputs, transactionOutputs, locktime);
		return new TransactionBuildResult(transaction, nextIndex);
	}

	/**
	 * Extracts the InputTransaction(s) from the given string.
	 * 
	 */
	private TransactionInputBuildResult deserializeTransactionInputs(byte[] data, int count, int dataStartIndex) {
		int nextIndex = dataStartIndex;
		List<TransactionInput> transactionInputs = new ArrayList<TransactionInput>(count);

		TransactionInput transactionInput;
		for (int i = 0; i < count; i++) {
			byte[] transactionId = ByteUtil.getBytes(data, nextIndex, TRANSACTION_ID_SIZE);
			Transaction tx;
			if (Arrays.equals(transactionId, voidTransaction.getId())) {
				tx = voidTransaction;
			} else {
				tx = new Transaction(transactionId);
			}
			nextIndex += TRANSACTION_ID_SIZE;
			int outputIndex = ByteUtil.getInt(data, nextIndex, OUTPUT_INDEX_SIZE);
			nextIndex += OUTPUT_INDEX_SIZE;
			int scriptSigSizeSize = HexStringUtil.getLengthOfVarInt(data, nextIndex);
			BigInteger scriptSigSize = HexStringUtil.parseVarInt(ByteUtil.getBytes(data, nextIndex, scriptSigSizeSize));
			nextIndex += scriptSigSizeSize;
			byte[] scriptSig = ByteUtil.getBytes(data, nextIndex, scriptSigSize.intValue());
			nextIndex += scriptSigSize.intValue();
			long sequence = ByteUtil.getLong(data, nextIndex, SEQUENCE_SIZE);
			nextIndex += SEQUENCE_SIZE;

			transactionInput = new TransactionInput(tx, outputIndex, scriptSig, sequence);
			transactionInputs.add(transactionInput);
		}

		TransactionInputBuildResult transactionInputBuildResult = new TransactionInputBuildResult(transactionInputs, nextIndex);
		return transactionInputBuildResult;
	}

	/**
	 * Extracts the OutputTransaction(s) from the given string.
	 * 
	 */
	private TransactionOutputBuildResult deserializeTransactionOutputs(byte[] data, int count, int dataStartIndex) {
		int nextIndex = dataStartIndex;
		List<TransactionOutput> transactionOutputs = new ArrayList<TransactionOutput>(count);

		TransactionOutput transactionOutput;
		for (int i = 0; i < count; i++) {
			BigInteger amount = ByteUtil.getBigInteger(data, nextIndex, AMOUNT_SIZE);
			nextIndex += AMOUNT_SIZE;
			int scriptPubKeySizeSize = HexStringUtil.getLengthOfVarInt(data, nextIndex);
			BigInteger scriptPubKeySize = HexStringUtil.parseVarInt(ByteUtil.getBytes(data, nextIndex, scriptPubKeySizeSize));
			nextIndex += scriptPubKeySizeSize;
			byte[] scriptPubKey = ByteUtil.getBytes(data, nextIndex, scriptPubKeySize.intValue());
			nextIndex += scriptPubKeySize.intValue();

			transactionOutput = new TransactionOutput(amount, scriptPubKey);
			transactionOutputs.add(transactionOutput);
		}

		TransactionOutputBuildResult transactionOutputBuildResult = new TransactionOutputBuildResult(transactionOutputs, nextIndex);
		return transactionOutputBuildResult;
	}

	/**
	 * Converts the given String of transactions data to its object form in a List.
	 * 
	 */
	@Override
	public List<Transaction> deserializeMultipleTransactions(byte[] transactionsData) {
		int nextIndex = 0;
		int transactionsCountSize = HexStringUtil.getLengthOfVarInt(transactionsData, nextIndex);
		int transactionsCount = ByteUtil.getInt(transactionsData, nextIndex, transactionsCountSize);
		nextIndex += transactionsCountSize;
		List<Transaction> transactions = new ArrayList<Transaction>(transactionsCount);
		for (int i = 0; i < transactionsCount; i++) {
			TransactionBuildResult result = Transaction.buildTransaction(transactionsData, nextIndex);
			transactions.add(result.getTransaction());
			nextIndex = result.getNextIndex();
		}
		return transactions;
	}
}
