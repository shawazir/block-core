package com.example.blockchain.domain.transaction.pool;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.example.blockchain.dao.UnconfirmedTransactionsDao;
import com.example.blockchain.domain.clock.NetworkClock;
import com.example.blockchain.domain.transaction.Transaction;
import com.example.blockchain.network.NetworkInterface;

public class ProfitAwareTransactionsPool extends TransactionsPool {

	public ProfitAwareTransactionsPool(NetworkClock networkClock, NetworkInterface networkInterface, UnconfirmedTransactionsDao unconfirmedTransactionsDao) {

		super(networkClock, networkInterface, unconfirmedTransactionsDao);
		List<PoolTransaction> dbList = unconfirmedTransactionsDao.findAll();
		list = new LinkedList<>();
		for (PoolTransaction poolTransaction : dbList) {
			addPoolTransaction(poolTransaction, false);
		}
		removeExpiredTransactions();
	}

	@Override
	public boolean addTransaction(Transaction tx) {
		return addTransaction(tx, false);
	}

	@Override
	public boolean addTransaction(Transaction tx, boolean owned) {
		PoolTransaction poolTransaction = new PoolTransaction(tx, new Date(), owned);
		return addPoolTransaction(poolTransaction, true);
	}

	private boolean addPoolTransaction(PoolTransaction poolTransaction, boolean save) {
		// TODO Validate the transaction

		boolean transactionAlreadyExists = (getPoolTransaction(poolTransaction.getTransaction().getId()) != null);
		if (transactionAlreadyExists) {
			return false;
		}

		if (list.size() == 0) {
			list.add(poolTransaction);
		} else {
			boolean txAdded = false;
			for (int i = 0; i < list.size(); i++) {
				boolean hasPriorityDueToBeingOwned = poolTransaction.isOwned() && !list.get(i).isOwned();
				boolean feesPerByteIsLarger = poolTransaction.getTransaction().getFeesPerByte() > list.get(i).getTransaction().getFeesPerByte();
				if (hasPriorityDueToBeingOwned || (feesPerByteIsLarger && !list.get(i).isOwned())) {
					list.add(i, poolTransaction);
					txAdded = true;
					break;
				}
			}
			if (!txAdded) {
				list.add(poolTransaction);
			}
		}
		if (save) {
			unconfirmedTransactionsDao.save(poolTransaction);
		}
		return true;
	}

	@Override
	public int addTransactions(Transaction[] transactions) {
		int count = 0;
		for (Transaction tx : transactions) {
			boolean added = addTransaction(tx);
			if (added) {
				count++;
			}
		}
		return count;
	}
}
