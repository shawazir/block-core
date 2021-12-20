package com.example.blockchain.domain.transaction.builder;

import java.util.List;

import com.example.blockchain.domain.transaction.TransactionOutput;

public class TransactionOutputBuildResult {

	private List<TransactionOutput> transactionOutputs;
	private int nextIndex;

	public TransactionOutputBuildResult(List<TransactionOutput> transactionOutputs, int nextIndex) {
		this.transactionOutputs = transactionOutputs;
		this.nextIndex = nextIndex;
	}

	// GETTERS & SETTERS // -----------------------------------------

	public List<TransactionOutput> getTransactionOutputs() {
		return transactionOutputs;
	}

	public int getNextIndex() {
		return nextIndex;
	}
}
