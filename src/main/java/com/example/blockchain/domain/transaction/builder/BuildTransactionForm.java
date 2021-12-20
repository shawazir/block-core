package com.example.blockchain.domain.transaction.builder;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;

import com.example.blockchain.domain.address.Address;
import com.example.blockchain.domain.transaction.Transaction;
import com.example.blockchain.domain.transaction.script.TransactionScriptsType;

public class BuildTransactionForm {

	private TransactionScriptsType scriptsType;
	private Transaction[] inputTxs;
	private int[] outputIndices;
	private KeyPair[] unlockingKeyPairs;
	private BigInteger[] amounts;
	private PublicKey[] lockingPublicKeys;
	private Address[] lockingAddresses;
	private long locktime;

	public BuildTransactionForm() {

	}

	public BuildTransactionForm(TransactionScriptsType scriptsType, Transaction[] inputTxs, int[] outputIndices, KeyPair[] unlockingKeyPairs,
			BigInteger[] amounts, PublicKey[] lockingPublicKeys, Address[] lockingAddresses, long locktime) {
		this.scriptsType = scriptsType;
		this.inputTxs = inputTxs;
		this.outputIndices = outputIndices;
		this.unlockingKeyPairs = unlockingKeyPairs;
		this.amounts = amounts;
		this.lockingPublicKeys = lockingPublicKeys;
		this.lockingAddresses = lockingAddresses;
		this.locktime = locktime;
	}

	// Builder Methods // -------------------------------------------

	public BuildTransactionForm scriptsType(TransactionScriptsType scriptsType) {
		this.setScriptsType(scriptsType);
		return this;
	}

	public BuildTransactionForm inputTxs(Transaction[] inputTxs) {
		this.setInputTxs(inputTxs);
		return this;
	}

	public BuildTransactionForm outputIndices(int[] outputIndices) {
		this.setOutputIndices(outputIndices);
		return this;
	}

	public BuildTransactionForm unlockingKeyPairs(KeyPair[] unlockingKeyPairs) {
		this.setUnlockingKeyPairs(unlockingKeyPairs);
		return this;
	}

	public BuildTransactionForm amounts(BigInteger[] amounts) {
		this.setAmounts(amounts);
		return this;
	}

	public BuildTransactionForm lockingPublicKeys(PublicKey[] lockingPublicKeys) {
		this.setLockingPublicKeys(lockingPublicKeys);
		return this;
	}

	public BuildTransactionForm lockingAddresses(Address[] lockingAddresses) {
		this.setLockingAddresses(lockingAddresses);
		return this;
	}

	public BuildTransactionForm locktime(long locktime) {
		this.setLocktime(locktime);
		return this;
	}

	// GETTERS & SETTERS // -----------------------------------------

	public TransactionScriptsType getScriptsType() {
		return scriptsType;
	}

	public void setScriptsType(TransactionScriptsType scriptsType) {
		this.scriptsType = scriptsType;
	}

	public Transaction[] getInputTxs() {
		return inputTxs;
	}

	public void setInputTxs(Transaction[] inputTxs) {
		this.inputTxs = inputTxs;
	}

	public int[] getOutputIndices() {
		return outputIndices;
	}

	public void setOutputIndices(int[] outputIndices) {
		this.outputIndices = outputIndices;
	}

	public KeyPair[] getUnlockingKeyPairs() {
		return unlockingKeyPairs;
	}

	public void setUnlockingKeyPairs(KeyPair[] unlockingKeyPairs) {
		this.unlockingKeyPairs = unlockingKeyPairs;
	}

	public BigInteger[] getAmounts() {
		return amounts;
	}

	public void setAmounts(BigInteger[] amounts) {
		this.amounts = amounts;
	}

	public PublicKey[] getLockingPublicKeys() {
		return lockingPublicKeys;
	}

	public void setLockingPublicKeys(PublicKey[] lockingPublicKeys) {
		this.lockingPublicKeys = lockingPublicKeys;
	}

	public Address[] getLockingAddresses() {
		return lockingAddresses;
	}

	public void setLockingAddresses(Address[] lockingAddresses) {
		this.lockingAddresses = lockingAddresses;
	}

	public long getLocktime() {
		return locktime;
	}

	public void setLocktime(long locktime) {
		this.locktime = locktime;
	}
}
