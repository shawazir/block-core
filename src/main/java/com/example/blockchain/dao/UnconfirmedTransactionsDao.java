package com.example.blockchain.dao;

import java.util.List;

import com.example.blockchain.domain.transaction.pool.TransactionsPool.PoolTransaction;

public interface UnconfirmedTransactionsDao {

	List<PoolTransaction> findAll();

	void save(PoolTransaction poolTransaction);

	void save(List<PoolTransaction> poolTransactions);

	boolean delete(PoolTransaction poolTransaction);

	int delete(List<PoolTransaction> poolTransactions);
}
