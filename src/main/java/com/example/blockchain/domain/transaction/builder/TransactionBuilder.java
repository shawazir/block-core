package com.example.blockchain.domain.transaction.builder;

import java.util.List;

import com.example.blockchain.domain.transaction.Transaction;

public interface TransactionBuilder {

	Transaction buildCoinbaseTransaction(BuildCoinbaseTransactionForm buildCoinbaseTransactionForm);

	Transaction buildTransaction(BuildTransactionForm buildTransactionForm);

	boolean validateTransaction(Transaction transaction);

	int calculateTransactionSize(Transaction transaction);

	TransactionSerializeResult serializeTransaction(Transaction transaction);

	byte[] serializeMultipleTransactions(List<Transaction> transactions);

	Transaction deserializeTransaction(byte[] transactionData);

	TransactionBuildResult deserializeTransaction(byte[] data, int dataStartIndex);

	List<Transaction> deserializeMultipleTransactions(byte[] transactionsData);
}
