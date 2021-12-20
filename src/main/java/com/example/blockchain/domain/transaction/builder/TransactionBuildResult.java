package com.example.blockchain.domain.transaction.builder;

import com.example.blockchain.domain.transaction.Transaction;

public class TransactionBuildResult {

	private Transaction transaction;
	private int nextIndex;

	public TransactionBuildResult(Transaction transaction, int nextIndex) {
		this.transaction = transaction;
		this.nextIndex = nextIndex;
	}

	// GETTERS & SETTERS // -----------------------------------------

	public Transaction getTransaction() {
		return transaction;
	}

	public int getNextIndex() {
		return nextIndex;
	}
}
