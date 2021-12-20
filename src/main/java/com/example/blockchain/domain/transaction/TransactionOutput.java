package com.example.blockchain.domain.transaction;

import java.math.BigInteger;

public class TransactionOutput {

	private BigInteger amount;
	private byte[] scriptPubKey;

	public TransactionOutput() {

	}

	public TransactionOutput(BigInteger amount, byte[] scriptPubKey) {
		this.amount = amount;
		this.scriptPubKey = scriptPubKey;
	}

	// GETTERS & SETTERS // -----------------------------------------

	public BigInteger getAmount() {
		return amount;
	}

	public void setAmount(BigInteger amount) {
		this.amount = amount;
	}

	public byte[] getScriptPubKey() {
		return scriptPubKey;
	}

	public void setScriptPubKey(byte[] scriptPubKey) {
		this.scriptPubKey = scriptPubKey;
	}

}
