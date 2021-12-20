package com.example.blockchain.domain.transaction.utxo;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;

import com.example.blockchain.domain.address.Address;
import com.example.blockchain.domain.transaction.Transaction;
import com.example.blockchain.util.ByteUtil;

public class UnspentTransactionOutput {

	private Transaction transaction;
	private int outputIndex;
	private BigInteger amount;

	public UnspentTransactionOutput(Transaction transaction, int outputIndex, BigInteger amount) {
		this.transaction = transaction;
		this.outputIndex = outputIndex;
		this.amount = amount;
	}

	public boolean isForwardedToAddress(Address address, PublicKey publicKey) {
		byte[] encodedPublicKey = publicKey.getEncoded();
		byte[] scriptPubKey = this.transaction.getOutputs().get(this.outputIndex).getScriptPubKey();
		// FIXME Not the optimal way to check
		if (ByteUtil.contains(scriptPubKey, address.getRipeMD160Hash()) || ByteUtil.contains(scriptPubKey, encodedPublicKey)) {
			return true;
		} else {
			return false;
		}
	}

	public Address isForwardedToAddresses(List<Address> addresses, List<PublicKey> publicKeys) {
		for (int i = 0; i < addresses.size(); i++) {
			if (isForwardedToAddress(addresses.get(i), publicKeys.get(i))) {
				return addresses.get(i);
			}
		}
		return null;
	}

	// Object METHODS // --------------------------------------------

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + outputIndex;
		result = prime * result + ((transaction.getId() == null) ? 0 : transaction.getId().hashCode());
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
		UnspentTransactionOutput other = (UnspentTransactionOutput) obj;
		if (outputIndex != other.outputIndex)
			return false;
		if (transaction.getId() == null) {
			if (other.transaction.getId() != null)
				return false;
		} else if (!Arrays.equals(transaction.getId(), other.transaction.getId()))
			return false;
		return true;
	}

	// GETTERS & SETTERS // -----------------------------------------

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public int getOutputIndex() {
		return outputIndex;
	}

	public void setOutputIndex(int outputIndex) {
		this.outputIndex = outputIndex;
	}

	public BigInteger getAmount() {
		return amount;
	}

	public void setAmount(BigInteger amount) {
		this.amount = amount;
	}
}
