package com.example.blockchain.domain.transaction.builder;

public class TransactionSerializeResult {

	private byte[] data;
	private int nextIndex;

	public TransactionSerializeResult(byte[] data, int nextIndex) {
		this.data = data;
		this.nextIndex = nextIndex;
	}

	// GETTERS & SETTERS // -----------------------------------------

	public byte[] getData() {
		return data;
	}

	public int getNextIndex() {
		return nextIndex;
	}
}
