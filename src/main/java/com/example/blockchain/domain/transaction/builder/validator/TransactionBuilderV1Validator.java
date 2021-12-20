package com.example.blockchain.domain.transaction.builder.validator;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;

import com.example.blockchain.domain.address.Address;
import com.example.blockchain.domain.transaction.Transaction;
import com.example.blockchain.domain.transaction.builder.BuildCoinbaseTransactionForm;
import com.example.blockchain.domain.transaction.builder.BuildTransactionForm;
import com.example.blockchain.domain.transaction.script.TransactionScriptsType;

public class TransactionBuilderV1Validator {

	private final static int MAX_NUMBER_OF_INPUTS_OUTPUTS = 255;
	private final static int MAX_OUTPUT_INDEX = 254; // Since the size is 1 byte and the index of the 255th item is 254
	private final static BigInteger MAX_AMOUNT = new BigInteger("ffffffffffffffff", 16);
	private final static long MAX_LOCKTIME = new BigInteger("ffffffff", 16).longValue();
	private final static long MAX_BLOCK_HEIGHT = new BigInteger("500000000").longValue();

	public BuildTransactionFormValidationResult validateBuildTransactionForm(BuildTransactionForm buildTransactionForm) {
		BuildTransactionFormValidationResult result = validateScriptsType(buildTransactionForm.getScriptsType());
		if (result != BuildTransactionFormValidationResult.SUCCESS) {
			return result;
		}

		result = validateInputTxs(buildTransactionForm.getInputTxs());
		if (result != BuildTransactionFormValidationResult.SUCCESS) {
			return result;
		}

		result = validateOutputIndices(buildTransactionForm.getOutputIndices());
		if (result != BuildTransactionFormValidationResult.SUCCESS) {
			return result;
		}

		result = validateUnlockingKeyPairs(buildTransactionForm.getUnlockingKeyPairs());
		if (result != BuildTransactionFormValidationResult.SUCCESS) {
			return result;
		}

		result = validateCountOfInputs(buildTransactionForm.getInputTxs(), buildTransactionForm.getOutputIndices(),
				buildTransactionForm.getUnlockingKeyPairs());
		if (result != BuildTransactionFormValidationResult.SUCCESS) {
			return result;
		}

		result = validateAmounts(buildTransactionForm.getAmounts());
		if (result != BuildTransactionFormValidationResult.SUCCESS) {
			return result;
		}

		result = validateLockingData(buildTransactionForm.getScriptsType(), buildTransactionForm.getLockingPublicKeys(),
				buildTransactionForm.getLockingAddresses());
		if (result != BuildTransactionFormValidationResult.SUCCESS) {
			return result;
		}

		result = validateCountOfOutputs(buildTransactionForm.getScriptsType(), buildTransactionForm.getAmounts(), buildTransactionForm.getLockingPublicKeys(),
				buildTransactionForm.getLockingAddresses());
		if (result != BuildTransactionFormValidationResult.SUCCESS) {
			return result;
		}

		result = validateLocktime(buildTransactionForm.getLocktime());
		if (result != BuildTransactionFormValidationResult.SUCCESS) {
			return result;
		}

		return BuildTransactionFormValidationResult.SUCCESS;
	}

	public BuildTransactionFormValidationResult validateBuildCoinbaseTransactionForm(BuildCoinbaseTransactionForm buildCoinbaseTransactionForm) {
		BuildTransactionFormValidationResult result = validateScriptsType(buildCoinbaseTransactionForm.getScriptsType());
		if (result != BuildTransactionFormValidationResult.SUCCESS) {
			return result;
		}

		result = validateBlockHeight(buildCoinbaseTransactionForm.getBlockHeight());
		if (result != BuildTransactionFormValidationResult.SUCCESS) {
			return result;
		}

		result = validateAmount(buildCoinbaseTransactionForm.getAmount());
		if (result != BuildTransactionFormValidationResult.SUCCESS) {
			return result;
		}

		result = validateLockingData(buildCoinbaseTransactionForm.getScriptsType(), buildCoinbaseTransactionForm.getLockingPublicKey(),
				buildCoinbaseTransactionForm.getLockingAddress());
		if (result != BuildTransactionFormValidationResult.SUCCESS) {
			return result;
		}

		return BuildTransactionFormValidationResult.SUCCESS;
	}

	private BuildTransactionFormValidationResult validateScriptsType(TransactionScriptsType scriptsType) {
		// Validates scriptsType is not null
		if (scriptsType == null) {
			return BuildTransactionFormValidationResult.INVALID_SCRIPTS_TYPE;
		}

		return BuildTransactionFormValidationResult.SUCCESS;
	}

	private BuildTransactionFormValidationResult validateInputTxs(Transaction[] inputTxs) {
		// Validates inputTxs is not null or empty
		if (inputTxs == null || inputTxs.length == 0) {
			return BuildTransactionFormValidationResult.INVALID_TRANSACTIONS;
		}

		// Validates the values in inputTxs are not null
		for (Transaction transaction : inputTxs) {
			if (transaction == null) {
				return BuildTransactionFormValidationResult.INVALID_TRANSACTIONS;
			}
		}

		return BuildTransactionFormValidationResult.SUCCESS;
	}

	private BuildTransactionFormValidationResult validateOutputIndices(int[] outputIndices) {
		// Validates outputIndices is not null or empty
		if (outputIndices == null || outputIndices.length == 0) {
			return BuildTransactionFormValidationResult.INVALID_OUTPUT_INDICES;
		}

		// Validates the values in outputIndices are not negative or too large
		for (int outputIndex : outputIndices) {
			if (outputIndex < 0 || outputIndex > MAX_OUTPUT_INDEX) {
				return BuildTransactionFormValidationResult.INVALID_OUTPUT_INDICES;
			}
		}

		return BuildTransactionFormValidationResult.SUCCESS;
	}

	private BuildTransactionFormValidationResult validateUnlockingKeyPairs(KeyPair[] unlockingKeyPairs) {
		// Validates unlockingKeyPairs is not null or empty
		if (unlockingKeyPairs == null || unlockingKeyPairs.length == 0) {
			return BuildTransactionFormValidationResult.INVALID_UNLOCKING_KEY_PAIRS;
		}

		// Validates the values in unlockingKeyPairs are not null
		for (KeyPair unlockingKeyPair : unlockingKeyPairs) {
			if (unlockingKeyPair == null) {
				return BuildTransactionFormValidationResult.INVALID_UNLOCKING_KEY_PAIRS;
			}
		}

		return BuildTransactionFormValidationResult.SUCCESS;
	}

	private BuildTransactionFormValidationResult validateCountOfInputs(Transaction[] inputTxs, int[] outputIndices, KeyPair[] unlockingKeyPairs) {
		if (inputTxs.length > MAX_NUMBER_OF_INPUTS_OUTPUTS || outputIndices.length > MAX_NUMBER_OF_INPUTS_OUTPUTS
				|| unlockingKeyPairs.length > MAX_NUMBER_OF_INPUTS_OUTPUTS) {
			return BuildTransactionFormValidationResult.TOO_MANY_INPUTS;
		} else if (inputTxs.length != outputIndices.length || outputIndices.length != unlockingKeyPairs.length) {
			return BuildTransactionFormValidationResult.MISMATCHING_INPUTS_COUNT;
		}

		return BuildTransactionFormValidationResult.SUCCESS;
	}

	private BuildTransactionFormValidationResult validateAmounts(BigInteger[] amounts) {
		// Validates amounts is not null or empty
		if (amounts == null || amounts.length == 0) {
			return BuildTransactionFormValidationResult.INVALID_AMOUNTS;
		}

		// Validates the values in amounts are not null or negative or too large
		for (BigInteger amount : amounts) {
			BuildTransactionFormValidationResult result = validateAmount(amount);
			if (result != BuildTransactionFormValidationResult.SUCCESS) {
				return result;
			}
		}

		return BuildTransactionFormValidationResult.SUCCESS;
	}

	private BuildTransactionFormValidationResult validateAmount(BigInteger amount) {
		if (amount == null || amount.compareTo(MAX_AMOUNT) > 0 || amount.compareTo(BigInteger.ONE) < 0) {
			return BuildTransactionFormValidationResult.INVALID_AMOUNTS;
		}

		return BuildTransactionFormValidationResult.SUCCESS;
	}

	private BuildTransactionFormValidationResult validateLockingData(TransactionScriptsType scriptsType, PublicKey[] lockingPublicKeys,
			Address[] lockingAddresses) {

		if (scriptsType == TransactionScriptsType.PAY_TO_PUBLIC_KEY) {
			return validateLockingPublicKeys(lockingPublicKeys);
		} else if (scriptsType == TransactionScriptsType.PAY_TO_PUBLIC_KEY_HASH) {
			return validateLockingAddresses(lockingAddresses);
		} else {
			throw new RuntimeException("Unsupported Transaction Scripts Type");
		}
	}

	private BuildTransactionFormValidationResult validateLockingData(TransactionScriptsType scriptsType, PublicKey lockingPublicKey, Address lockingAddress) {

		if (scriptsType == TransactionScriptsType.PAY_TO_PUBLIC_KEY) {
			return validateLockingPublicKey(lockingPublicKey);
		} else if (scriptsType == TransactionScriptsType.PAY_TO_PUBLIC_KEY_HASH) {
			return validateLockingAddress(lockingAddress);
		} else {
			throw new RuntimeException("Unsupported Transaction Scripts Type");
		}
	}

	private BuildTransactionFormValidationResult validateLockingPublicKeys(PublicKey[] lockingPublicKeys) {
		// Validates lockingPublicKeys is not null or empty
		if (lockingPublicKeys == null || lockingPublicKeys.length == 0) {
			return BuildTransactionFormValidationResult.INVALID_LOCKING_PUBLIC_KEYS;
		}

		// Validates the values in lockingPublicKeys are not null
		for (PublicKey lockingPublicKey : lockingPublicKeys) {
			BuildTransactionFormValidationResult result = validateLockingPublicKey(lockingPublicKey);
			if (result != BuildTransactionFormValidationResult.SUCCESS) {
				return result;
			}
		}

		return BuildTransactionFormValidationResult.SUCCESS;
	}

	private BuildTransactionFormValidationResult validateLockingPublicKey(PublicKey lockingPublicKey) {
		if (lockingPublicKey == null) {
			return BuildTransactionFormValidationResult.INVALID_LOCKING_PUBLIC_KEYS;
		}

		return BuildTransactionFormValidationResult.SUCCESS;
	}

	private BuildTransactionFormValidationResult validateLockingAddresses(Address[] lockingAddresses) {
		// Validates lockingAddresses is not null or empty
		if (lockingAddresses == null || lockingAddresses.length == 0) {
			return BuildTransactionFormValidationResult.INVALID_LOCKING_ADDRESSES;
		}

		// Validates the values in lockingAddresses are not null
		for (Address lockingAddress : lockingAddresses) {
			BuildTransactionFormValidationResult result = validateLockingAddress(lockingAddress);
			if (result != BuildTransactionFormValidationResult.SUCCESS) {
				return result;
			}
		}

		return BuildTransactionFormValidationResult.SUCCESS;
	}

	private BuildTransactionFormValidationResult validateLockingAddress(Address lockingAddress) {
		if (lockingAddress == null) {
			return BuildTransactionFormValidationResult.INVALID_LOCKING_ADDRESSES;
		}

		return BuildTransactionFormValidationResult.SUCCESS;
	}

	private BuildTransactionFormValidationResult validateCountOfOutputs(TransactionScriptsType scriptsType, BigInteger[] amounts, PublicKey[] lockingPublicKeys,
			Address[] lockingAddresses) {

		if (scriptsType == TransactionScriptsType.PAY_TO_PUBLIC_KEY) {
			if (amounts.length > MAX_NUMBER_OF_INPUTS_OUTPUTS || lockingPublicKeys.length > MAX_NUMBER_OF_INPUTS_OUTPUTS) {
				return BuildTransactionFormValidationResult.TOO_MANY_OUTPUTS;
			} else if (amounts.length != lockingPublicKeys.length) {
				return BuildTransactionFormValidationResult.MISMATCHING_OUTPUTS_COUNT;
			}
		} else if (scriptsType == TransactionScriptsType.PAY_TO_PUBLIC_KEY_HASH) {
			if (amounts.length > MAX_NUMBER_OF_INPUTS_OUTPUTS || lockingAddresses.length > MAX_NUMBER_OF_INPUTS_OUTPUTS) {
				return BuildTransactionFormValidationResult.TOO_MANY_OUTPUTS;
			} else if (amounts.length != lockingAddresses.length) {
				return BuildTransactionFormValidationResult.MISMATCHING_OUTPUTS_COUNT;
			}
		} else {
			throw new RuntimeException("Unsupported Transaction Scripts Type");
		}

		return BuildTransactionFormValidationResult.SUCCESS;
	}

	private BuildTransactionFormValidationResult validateLocktime(long locktime) {
		if (locktime < 0 || locktime > MAX_LOCKTIME) {
			return BuildTransactionFormValidationResult.INVALID_LOCKTIME;
		}

		return BuildTransactionFormValidationResult.SUCCESS;
	}

	private BuildTransactionFormValidationResult validateBlockHeight(long blockHeight) {
		if (blockHeight < 0 || blockHeight > MAX_BLOCK_HEIGHT) {
			return BuildTransactionFormValidationResult.INVALID_BLOCK_HEIGHT;
		}

		return BuildTransactionFormValidationResult.SUCCESS;
	}
}
