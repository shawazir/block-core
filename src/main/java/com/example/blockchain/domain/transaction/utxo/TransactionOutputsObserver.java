package com.example.blockchain.domain.transaction.utxo;

import java.util.List;

public interface TransactionOutputsObserver {

	void outputsUpdated(List<UnspentTransactionOutput> addedOutputs, List<UnspentTransactionOutput> deletedOutputs);
}
