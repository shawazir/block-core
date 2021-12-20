package com.example.blockchain.domain.transaction.builder;

import java.util.List;

import com.example.blockchain.domain.transaction.TransactionInput;

public class TransactionInputBuildResult {

	private List<TransactionInput> transactionInputs;
	private int nextIndex;

	public TransactionInputBuildResult(List<TransactionInput> transactionInputs, int nextIndex) {
		this.transactionInputs = transactionInputs;
		this.nextIndex = nextIndex;
	}

	// GETTERS & SETTERS // -----------------------------------------

	public List<TransactionInput> getTransactionInputs() {
		return transactionInputs;
	}

	public int getNextIndex() {
		return nextIndex;
	}
}
