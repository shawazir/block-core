package com.example.blockchain.dao;

import java.util.List;

import com.example.blockchain.domain.address.Address;
import com.example.blockchain.domain.transaction.utxo.UnspentTransactionOutput;

public interface UnspentTransactionOutputDao {

	UnspentTransactionOutput find(byte[] txId, int outputIndex);

	UnspentTransactionOutput findForAddress(byte[] txId, int outputIndex, Address address);

	List<UnspentTransactionOutput> findAllForAddress(Address address);

	void save(UnspentTransactionOutput unspentTransactionOutput);

	void save(List<UnspentTransactionOutput> unspentTransactionOutputs);

	void saveForAddress(UnspentTransactionOutput unspentTransactionOutput, Address address);

	void saveForAddress(List<UnspentTransactionOutput> unspentTransactionOutputs, Address address);

	boolean delete(UnspentTransactionOutput unspentTransactionOutput);

	int delete(List<UnspentTransactionOutput> unspentTransactionOutputs);

	boolean deleteForAddress(UnspentTransactionOutput unspentTransactionOutput, Address address);

	int deleteForAddress(List<UnspentTransactionOutput> unspentTransactionOutputs, Address address);
}
