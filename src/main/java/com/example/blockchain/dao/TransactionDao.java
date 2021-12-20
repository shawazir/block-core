package com.example.blockchain.dao;

import com.example.blockchain.domain.transaction.Transaction;

public interface TransactionDao {

	/**
	 * Finds the transaction with the given ID.
	 * 
	 */
	Transaction find(byte[] txId);

	/**
	 * Loads the data within the given transaction.
	 * 
	 */
	void load(Transaction tx);

	/**
	 * Saves the given transaction.
	 * 
	 */
	void save(Transaction transaction);

	/**
	 * Saves the given transactions.
	 * 
	 */
	void save(Transaction[] transactions);

	/**
	 * Deletes the given transaction.
	 * 
	 */
	boolean delete(Transaction transaction);

	/**
	 * Deletes the given transactions and returns the count of transactions which
	 * were deleted.
	 * 
	 */
	int delete(Transaction[] transactions);
}
