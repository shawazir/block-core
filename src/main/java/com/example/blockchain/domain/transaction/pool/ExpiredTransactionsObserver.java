package com.example.blockchain.domain.transaction.pool;

import com.example.blockchain.domain.transaction.Transaction;

public interface ExpiredTransactionsObserver {

	void transactionExpired(Transaction expiredTransaction);
}
