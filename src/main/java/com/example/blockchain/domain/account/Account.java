package com.example.blockchain.domain.account;

import java.math.BigInteger;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import com.example.blockchain.domain.address.Address;
import com.example.blockchain.domain.transaction.utxo.UnspentTransactionOutput;

public class Account {

	private String name;
	private KeyPair keyPair;
	private Address address;
	private List<UnspentTransactionOutput> unspentTransactionOutputs;

	public Account(String name, KeyPair keyPair) {
		this.name = name;
		this.keyPair = keyPair;
		this.address = new Address(keyPair.getPublic());
		this.unspentTransactionOutputs = new ArrayList<>();
	}

	public void addUnspentTransactionOutput(UnspentTransactionOutput unspentTransactionOutput) {
		unspentTransactionOutputs.add(unspentTransactionOutput);
	}

	public void addUnspentTransactionOutputs(List<UnspentTransactionOutput> unspentTransactionOutputs) {
		for (UnspentTransactionOutput output : unspentTransactionOutputs) {
			addUnspentTransactionOutput(output);
		}
	}

	public void removeUnspentTransactionOutput(UnspentTransactionOutput unspentTransactionOutput) {
		unspentTransactionOutputs.remove(unspentTransactionOutput);
	}

	public void removeUnspentTransactionOutputs(List<UnspentTransactionOutput> unspentTransactionOutputs) {
		for (UnspentTransactionOutput output : unspentTransactionOutputs) {
			removeUnspentTransactionOutput(output);
		}
	}

	public BigInteger getBalance() {
		BigInteger balance = BigInteger.ZERO;
		for (UnspentTransactionOutput output : unspentTransactionOutputs) {
			balance = balance.add(output.getAmount());
		}
		return balance;
	}

	// GETTERS & SETTERS // -----------------------------------------

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public KeyPair getKeyPair() {
		return keyPair;
	}

	public void setKeyPair(KeyPair keyPair) {
		this.keyPair = keyPair;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public List<UnspentTransactionOutput> getUnspentTransactionOutputs() {
		return unspentTransactionOutputs;
	}
}
