package com.example.blockchain.domain.transaction.pool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.blockchain.Callback;
import com.example.blockchain.dao.UnconfirmedTransactionsDao;
import com.example.blockchain.domain.clock.NetworkClock;
import com.example.blockchain.domain.transaction.Transaction;
import com.example.blockchain.network.NetworkCallback;
import com.example.blockchain.network.NetworkInterface;
import com.example.blockchain.network.NodeAvailabilitySubscriber;
import com.example.blockchain.network.handler.GetResponse;

public abstract class TransactionsPool implements NodeAvailabilitySubscriber {

	private static final Logger log = LoggerFactory.getLogger(TransactionsPool.class);

	private static final int RETRIEVAL_DELAY_AFTER_SUCCESS = 1000 * 60 * 60 * 6; // Six hours
	private static final int RETRIEVAL_DELAY_AFTER_FAILURE = 1000 * 60 * 60; // One hour
	static final long TRANSACTION_EXPIRY_PERIOD = 1000 * 60 * 60 * 24 * 14; // Two weeks
	static final long TRANSACTION_EXPIRY_CHECK_INTERVAL = 1000 * 60 * 60; // One hour
	private static final int MIN_NUMBER_OF_NODES = 1;

	private NetworkClock networkClock;
	NetworkInterface networkInterface;
	UnconfirmedTransactionsDao unconfirmedTransactionsDao;

	List<PoolTransaction> list;
	private Set<ExpiredTransactionsObserver> expiredTransactionsObservers = new HashSet<>();
	private Timer timer = new Timer();
	private boolean needsToUpdateWithEnoughNodes = false;

	TransactionsPool(NetworkClock networkClock, NetworkInterface networkInterface, UnconfirmedTransactionsDao unconfirmedTransactionsDao) {

		this.networkClock = networkClock;
		this.networkInterface = networkInterface;
		this.unconfirmedTransactionsDao = unconfirmedTransactionsDao;

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				removeExpiredTransactions();
			}
		}, TRANSACTION_EXPIRY_CHECK_INTERVAL, TRANSACTION_EXPIRY_CHECK_INTERVAL);
	}

	public void start() {
		start(Callback.createEmptyTypeVoid());
	}

	public void start(Callback<Void> callback) {
		networkInterface.subscribeNodeAvailabilitySubscriber(this, MIN_NUMBER_OF_NODES);
		startSynchingWithNetwork(callback);
	}

	/**
	 * Starts Retrieving the pending transactions from the network in order to add
	 * them to the pool if valid.
	 * 
	 */
	void startSynchingWithNetwork(Callback<Void> callback) {
		log.debug("Started retrieving pending transactions");
		NetworkCallback<GetResponse<List<Transaction>>> networkCallback = new NetworkCallback<GetResponse<List<Transaction>>>() {
			@Override
			public void onSuccess(GetResponse<List<Transaction>> response) {
				finishSynchingWithNetwork(response.getValues(), callback);
			}

			@Override
			public void onFailure(GetResponse<List<Transaction>> response) {
				scheduleSynchProcess(RETRIEVAL_DELAY_AFTER_FAILURE);
				log.info("Failed to retrieve pending transactions from network due to no replies");
				callback.onFailure(null);
			}
		};
		boolean requestWasSent = networkInterface.getPendingTransactions(MIN_NUMBER_OF_NODES, MIN_NUMBER_OF_NODES, networkCallback);
		if (!requestWasSent) {
			scheduleSynchProcess(RETRIEVAL_DELAY_AFTER_FAILURE);
			needsToUpdateWithEnoughNodes = true;
			log.info("Failed to retrieve pending transactions from network due to not enough nodes");
			callback.onFailure(null);
		}
	}

	private void finishSynchingWithNetwork(List<List<Transaction>> pendingTransactions, Callback<Void> callback) {
		// FIXME Validate the transactions before adding them
		Transaction[] pendingTransactionsArray = new Transaction[pendingTransactions.get(0).size()];
		for (int i = 0; i < pendingTransactions.get(0).size(); i++) {
			pendingTransactionsArray[i] = pendingTransactions.get(0).get(i);
		}
		removeExpiredTransactions();

		scheduleSynchProcess(RETRIEVAL_DELAY_AFTER_SUCCESS);
		log.info("Retrieved pending transactions (Count: {}) from network successfully", pendingTransactions.get(0).size());
		callback.onSuccess(null);
	}

	private void scheduleSynchProcess(long delay) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				startSynchingWithNetwork(Callback.createEmptyTypeVoid());
			}
		}, delay);
	}

	/**
	 * Registers the given ExpiredTransactionsObserver.
	 * 
	 */
	public void registerExpiredTransactionsObserver(ExpiredTransactionsObserver observer) {
		expiredTransactionsObservers.add(observer);
	}

	/**
	 * Unregisters the given ExpiredTransactionsObserver.
	 * 
	 */
	public void unregisterExpiredTransactionsObserver(ExpiredTransactionsObserver observer) {
		expiredTransactionsObservers.remove(observer);
	}

	private void notifyExpiredTransactionsObservers(Transaction expiredTransaction) {
		for (ExpiredTransactionsObserver observer : expiredTransactionsObservers) {
			observer.transactionExpired(expiredTransaction);
		}
	}

	/**
	 * Adds the given transaction to the pool.
	 * 
	 */
	public abstract boolean addTransaction(Transaction tx);

	/**
	 * Adds the given transaction to the pool.
	 * 
	 */
	public abstract boolean addTransaction(Transaction tx, boolean owned);

	/**
	 * Adds the given transaction to the pool.
	 * 
	 */
	public abstract int addTransactions(Transaction[] transactions);

	/**
	 * Removes the transaction with the given ID from the pool.
	 * 
	 */
	public boolean removeTransaction(byte[] id) {
		for (int i = 0; i < list.size(); i++) {
			if (Arrays.equals(list.get(i).getTransaction().getId(), id)) {
				unconfirmedTransactionsDao.delete(list.get(i));
				list.remove(i);
				return true;
			}
		}
		return false;
	}

	/**
	 * Removes the given transaction from the pool.
	 * 
	 */
	public boolean removeTransaction(Transaction tx) {
		return removeTransaction(tx.getId());
	}

	/**
	 * Removes the given transactions from the pool.
	 * 
	 */
	public void removeTransactions(Transaction[] transactions) {
		for (Transaction tx : transactions) {
			removeTransaction(tx);
		}
	}

	/**
	 * Removes expired transactions from the pool.
	 * 
	 */
	void removeExpiredTransactions() {
		Date networkTime = networkClock.getNetworkTime();
		ListIterator<PoolTransaction> iterator = list.listIterator();
		while (iterator.hasNext()) {
			PoolTransaction poolTransaction = iterator.next();
			if (poolTransaction.isExpired(networkTime)) {
				unconfirmedTransactionsDao.delete(poolTransaction);
				iterator.remove();
				notifyExpiredTransactionsObservers(poolTransaction.getTransaction());
			}
		}
	}

	/**
	 * Gets all candidate transactions.
	 * 
	 */
	public List<Transaction> getAllCandidateTransactions() {
		List<Transaction> candidateTxsList = new ArrayList<>(list.size());
		for (PoolTransaction poolTransaction : list) {
			candidateTxsList.add(poolTransaction.getTransaction());
		}
		return candidateTxsList;
	}

	/**
	 * Gets candidate transactions with the given maximum size.
	 * 
	 */
	public List<Transaction> getCandidateTransactions(int maxSize) {
		if (maxSize < 1) {
			throw new RuntimeException("Invalid Coinbase Size");
		}

		int accumulatedSize = 0;
		List<Transaction> candidateTxsList = new ArrayList<>();
		for (PoolTransaction poolTransaction : list) {
			Transaction tx = poolTransaction.getTransaction();
			if (accumulatedSize + tx.getSize() <= maxSize) {
				candidateTxsList.add(tx);
				accumulatedSize += tx.getSize();
			}
		}
		return candidateTxsList;
	}

	/**
	 * Gets the count of transactions.
	 * 
	 */
	public int getCount() {
		return list.size();
	}

	PoolTransaction getPoolTransaction(byte[] txId) {
		for (PoolTransaction poolTransaction : list) {
			if (Arrays.equals(poolTransaction.getTransaction().getId(), txId)) {
				return poolTransaction;
			}
		}
		return null;
	}

	Transaction getTransaction(int index) {
		return list.get(index).getTransaction();
	}

	@Override
	public void onAvailability() {
		if (needsToUpdateWithEnoughNodes) {
			needsToUpdateWithEnoughNodes = false;
			startSynchingWithNetwork(Callback.createEmptyTypeVoid());
		}
	}

	public static class PoolTransaction {

		private Transaction transaction;
		private Date addedOn;
		// FIXME There will be inconsistency if the owning account is deleted
		private boolean owned; // Specifies if the transaction is owned by one of the user accounts

		public PoolTransaction(Transaction transaction, Date addedOn, boolean owned) {
			this.transaction = transaction;
			this.addedOn = addedOn;
			this.owned = owned;
		}

		public boolean isExpired(Date networkTime) {
			long diffrenceInTime = networkTime.getTime() - addedOn.getTime();
			if (diffrenceInTime > TRANSACTION_EXPIRY_PERIOD) {
				return true;
			} else {
				return false;
			}
		}

		public Transaction getTransaction() {
			return transaction;
		}

		public Date getAddedOn() {
			return addedOn;
		}

		public boolean isOwned() {
			return owned;
		}
	}
}
