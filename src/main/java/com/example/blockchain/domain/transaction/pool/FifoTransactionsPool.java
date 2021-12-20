package com.example.blockchain.domain.transaction.pool;

import java.util.Date;

import com.example.blockchain.dao.UnconfirmedTransactionsDao;
import com.example.blockchain.domain.clock.NetworkClock;
import com.example.blockchain.domain.transaction.Transaction;
import com.example.blockchain.network.NetworkInterface;

public class FifoTransactionsPool extends TransactionsPool {

	public FifoTransactionsPool(NetworkClock networkClock, NetworkInterface networkInterface, UnconfirmedTransactionsDao unconfirmedTransactionsDao) {

		super(networkClock, networkInterface, unconfirmedTransactionsDao);
		list = unconfirmedTransactionsDao.findAll();
		removeExpiredTransactions();
	}

	@Override
	public boolean addTransaction(Transaction tx) {
		return addTransaction(tx, false);
	}

	@Override
	public boolean addTransaction(Transaction tx, boolean owned) {
		// TODO Validate the transaction

		boolean transactionAlreadyExists = (getPoolTransaction(tx.getId()) != null);
		if (transactionAlreadyExists) {
			return false;
		}

		PoolTransaction poolTransaction = new PoolTransaction(tx, new Date(), owned);
		list.add(poolTransaction);
		unconfirmedTransactionsDao.save(poolTransaction);
		return true;
	}

	@Override
	public int addTransactions(Transaction[] transactions) {
		int count = 0;
		for (Transaction tx : transactions) {
			boolean added = addTransaction(tx, false);
			if (added) {
				count++;
			}
		}
		return count;
	}
}
