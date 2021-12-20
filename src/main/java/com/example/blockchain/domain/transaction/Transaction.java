package com.example.blockchain.domain.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;

import com.example.blockchain.dao.TransactionDao;
import com.example.blockchain.domain.address.Address;
import com.example.blockchain.domain.transaction.builder.BuildCoinbaseTransactionForm;
import com.example.blockchain.domain.transaction.builder.BuildTransactionForm;
import com.example.blockchain.domain.transaction.builder.TransactionBuildResult;
import com.example.blockchain.domain.transaction.builder.TransactionBuilder;
import com.example.blockchain.domain.transaction.builder.TransactionBuilderV1;
import com.example.blockchain.domain.transaction.script.PayToPublicKeyHashScriptsBuilder;
import com.example.blockchain.domain.transaction.script.PayToPublicKeyScriptsBuilder;
import com.example.blockchain.domain.transaction.utxo.UnspentTransactionOutput;
import com.example.blockchain.util.ByteUtil;
import com.example.blockchain.util.ConvertUtil;
import com.example.blockchain.util.Sha256Util;

public class Transaction {

	private byte[] id;
	private short version;
	private List<TransactionInput> inputs;
	private List<TransactionOutput> outputs;
	private long locktime;

	private byte[] cachedTransactionData;
	private boolean loaded = false;
	private boolean isVoid = false;

	private static TransactionDao transactionDao;

	private static TransactionBuilder transactionBuilder = new TransactionBuilderV1(new PayToPublicKeyScriptsBuilder(), new PayToPublicKeyHashScriptsBuilder(),
			new Transaction(true));

	private Transaction(boolean foid) {
		if (foid) {
			this.id = new byte[32];
			this.isVoid = true;
		} else {
			throw new RuntimeException("Constructor cannot be invoked with false value");
		}
	}

	public Transaction(byte[] id) {
		this.id = id;
		this.loaded = false;
	}

	public Transaction(short version, List<TransactionInput> inputs, List<TransactionOutput> outputs, long locktime) {
		this.version = version;
		this.inputs = inputs;
		this.outputs = outputs;
		this.locktime = locktime;
		this.loaded = true;
		this.cachedTransactionData = transactionBuilder.serializeTransaction(this).getData();
		setId();
	}

	public Transaction(byte[] id, short version, List<TransactionInput> inputs, List<TransactionOutput> outputs, long locktime) {
		this.id = id;
		this.version = version;
		this.inputs = inputs;
		this.outputs = outputs;
		this.locktime = locktime;
		this.loaded = true;
		this.cachedTransactionData = transactionBuilder.serializeTransaction(this).getData();
	}

	/**
	 * Sets the TransactionDao.
	 *
	 */
	public static void setTransactionDao(TransactionDao transactionDao) {
		Transaction.transactionDao = transactionDao;
	}

	/**
	 * Creates a new coinbase transaction.
	 * 
	 */
	public static Transaction createCoinbaseTransaction(BuildCoinbaseTransactionForm buildCoinbaseTransactionForm) {
		return transactionBuilder.buildCoinbaseTransaction(buildCoinbaseTransactionForm);
	}

	/*
	 * Creates a new transaction.
	 * 
	 */
	public static Transaction createTransaction(BuildTransactionForm buildTransactionForm) {
		return transactionBuilder.buildTransaction(buildTransactionForm);
	}

	/**
	 * Builds a transaction from the given data.
	 * 
	 */
	public static Transaction buildTransaction(byte[] transactionData) {
		// TODO Handle invalid transactionData
		return transactionBuilder.deserializeTransaction(transactionData);
	}

	/**
	 * Builds a transaction from the given data starting at the given index.
	 * 
	 */
	public static TransactionBuildResult buildTransaction(byte[] data, int dataStartIndex) {
		// TODO Handle invalid transactionData
		return transactionBuilder.deserializeTransaction(data, dataStartIndex);
	}

	/**
	 * Builds the transaction ID for the data in the given byte array.
	 * 
	 */
	public static byte[] buildTransactionId(byte[] txData) {
		byte[] txId = Sha256Util.doubleHash(txData);
		return txId;
	}

	/**
	 * Builds the transaction ID for the data in the given string.
	 * 
	 */
	public static String buildTransactionId(String txData) {
		byte[] txDataByteArray = ConvertUtil.hexStringToByteArray(txData);
		byte[] txIdByteArray = buildTransactionId(txDataByteArray);
		return ConvertUtil.byteArrayToHexString(txIdByteArray);
	}

	/**
	 * Serializes the given list of transactions.
	 * 
	 */
	public static byte[] serializeMultipleTransactions(List<Transaction> transactions) {
		return transactionBuilder.serializeMultipleTransactions(transactions);
	}

	/**
	 * Deserializes the given transactions data.
	 * 
	 */
	public static List<Transaction> deserializeMultipleTransactions(byte[] transactionsData) {
		return transactionBuilder.deserializeMultipleTransactions(transactionsData);
	}

	/**
	 * Sets the ID of the transaction.
	 * 
	 */
	public void setId() {
		if (this.id == null) {
			byte[] txData = transactionBuilder.serializeTransaction(this).getData();
			this.cachedTransactionData = txData;
			this.id = buildTransactionId(txData);
		}
	}

	/**
	 * Serializes the transaction.
	 * 
	 */
	public byte[] serialize() {
		return transactionBuilder.serializeTransaction(this).getData();
	}

	/**
	 * Validates the transaction.
	 * 
	 */
	public boolean validate() {
		// TODO Implement this
		return false;
	}

	/**
	 * Loads the data of the transaction. If the data is already loaded, nothing is
	 * done.
	 * 
	 */
	public void load() {
		if (!loaded) {
			loadData();
		}
	}

	/**
	 * Loads the data of the transaction. If the data is already loaded, nothing is
	 * done.
	 * 
	 */
	private void loadData() {
		// TODO Handle multiple concurrent requests to load the data
		if (!loaded) {
			Transaction tx = transactionDao.find(this.id);
			this.version = tx.getVersion();
			this.inputs = tx.getInputs();
			this.outputs = tx.getOutputs();
			this.locktime = tx.getLocktime();
			this.loaded = true;
			this.cachedTransactionData = transactionBuilder.serializeTransaction(this).getData();
		}
	}

	/**
	 * Finds if the transaction is a coinbase transaction.
	 * 
	 */
	public boolean isCoinbase() {
		return inputs.get(0).getTransaction().isVoid;
	}

	/**
	 * Finds if the transaction can be included in a block.
	 * 
	 */
	public boolean isBlockable() {
		// TODO Implement this
		return false;
	}

	/**
	 * Gets the size of the transaction in bytes.
	 * 
	 */
	public int getSize() {
		return cachedTransactionData.length;
	}

	/**
	 * Gets the fees of the transaction.
	 * 
	 */
	public BigInteger getFees() {
		BigInteger fees = BigInteger.valueOf(0);
		if (isCoinbase()) {
			return fees;
		}

		for (TransactionInput input : inputs) {
			int outputIndex = input.getOutputIndex();
			fees = fees.add(input.getTransaction().getOutputs().get(outputIndex).getAmount());
		}
		for (TransactionOutput output : outputs) {
			fees = fees.subtract(output.getAmount());
		}
		return fees;
	}

	/**
	 * Gets the fees per byte.
	 * 
	 */
	public double getFeesPerByte() {
		if (isCoinbase()) {
			return 0;
		}

		BigDecimal fees = new BigDecimal(getFees());
		int size = getSize();
		return fees.divide(BigDecimal.valueOf(size), 3, RoundingMode.HALF_UP).doubleValue();
	}

	/**
	 * Gets the coordinates of the consumed transaction outputs.
	 * 
	 */
	public UnspentTransactionOutput[] getConsumedOutputs() {
		if (isCoinbase()) {
			return new UnspentTransactionOutput[0];
		} else {
			UnspentTransactionOutput[] consumedOutputs = new UnspentTransactionOutput[inputs.size()];
			for (int i = 0; i < inputs.size(); i++) {
				Transaction transaction = inputs.get(i).getTransaction();
				int outputIndex = inputs.get(i).getOutputIndex();
				BigInteger amount = inputs.get(i).getTransaction().getOutputs().get(outputIndex).getAmount();
				consumedOutputs[i] = new UnspentTransactionOutput(transaction, outputIndex, amount);
			}
			return consumedOutputs;
		}
	}

	/**
	 * Gets the coordinates of the produced transaction outputs.
	 * 
	 */
	public UnspentTransactionOutput[] getProducedOutputs() {
		UnspentTransactionOutput[] producedOutputs = new UnspentTransactionOutput[outputs.size()];
		for (int i = 0; i < outputs.size(); i++) {
			producedOutputs[i] = new UnspentTransactionOutput(this, i, outputs.get(i).getAmount());
		}
		return producedOutputs;
	}

	/**
	 * Computes the net outcome for the given address in the transaction.
	 * 
	 */
	public BigInteger computeNetOutcomeForAddress(Address address, PublicKey publicKey) {
		BigInteger outcome = BigInteger.ZERO;
		if (!this.isCoinbase()) {
			for (TransactionInput input : inputs) {
				Transaction tx = input.getTransaction();
				byte[] scriptPubKey = tx.getOutputs().get(input.getOutputIndex()).getScriptPubKey();
				BigInteger amount = tx.getOutputs().get(input.getOutputIndex()).getAmount();
				// FIXME Not the optimal way to check
				if (ByteUtil.contains(scriptPubKey, address.getRipeMD160Hash()) || ByteUtil.contains(scriptPubKey, publicKey.getEncoded())) {
					outcome = outcome.subtract(amount);
				}
			}
		}
		for (TransactionOutput output : outputs) {
			byte[] scriptPubKey = output.getScriptPubKey();
			BigInteger amount = output.getAmount();
			// FIXME Not the optimal way to check
			if (ByteUtil.contains(scriptPubKey, address.getRipeMD160Hash()) || ByteUtil.contains(scriptPubKey, publicKey.getEncoded())) {
				outcome = outcome.add(amount);
			}
		}
		return outcome;
	}

	// Object METHODS // --------------------------------------------

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(id);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Transaction other = (Transaction) obj;
		if (!Arrays.equals(id, other.id))
			return false;
		return true;
	}

	// GETTERS & SETTERS // -----------------------------------------

	public byte[] getId() {
		return id;
	}

	public short getVersion() {
		if (!loaded) {
			load();
		}
		return version;
	}

	public List<TransactionInput> getInputs() {
		if (!loaded) {
			load();
		}
		return inputs;
	}

	public List<TransactionOutput> getOutputs() {
		if (!loaded) {
			load();
		}
		return outputs;
	}

	public long getLocktime() {
		if (!loaded) {
			load();
		}
		return locktime;
	}
}
