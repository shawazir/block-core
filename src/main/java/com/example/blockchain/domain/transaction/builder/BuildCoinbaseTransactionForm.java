package com.example.blockchain.domain.transaction.builder;

import java.math.BigInteger;
import java.security.PublicKey;

import com.example.blockchain.domain.address.Address;
import com.example.blockchain.domain.transaction.script.TransactionScriptsType;

public class BuildCoinbaseTransactionForm {

	private TransactionScriptsType scriptsType;
	private int blockHeight;
	private String scriptSigText;
	private BigInteger amount;
	private PublicKey lockingPublicKey;
	private Address lockingAddress;

	public BuildCoinbaseTransactionForm() {

	}

	public BuildCoinbaseTransactionForm(TransactionScriptsType scriptsType, int blockHeight, String scriptSigText, BigInteger amount,
			PublicKey lockingPublicKey, Address lockingAddress) {
		this.scriptsType = scriptsType;
		this.blockHeight = blockHeight;
		this.scriptSigText = scriptSigText;
		this.amount = amount;
		this.lockingPublicKey = lockingPublicKey;
		this.lockingAddress = lockingAddress;
	}

	// Builder Methods // -------------------------------------------

	public BuildCoinbaseTransactionForm scriptsType(TransactionScriptsType scriptsType) {
		this.scriptsType = scriptsType;
		return this;
	}

	public BuildCoinbaseTransactionForm blockHeight(int blockHeight) {
		this.blockHeight = blockHeight;
		return this;
	}

	public BuildCoinbaseTransactionForm scriptSigText(String scriptSigText) {
		this.scriptSigText = scriptSigText;
		return this;
	}

	public BuildCoinbaseTransactionForm amount(BigInteger amount) {
		this.amount = amount;
		return this;
	}

	public BuildCoinbaseTransactionForm lockingPublicKey(PublicKey lockingPublicKey) {
		this.lockingPublicKey = lockingPublicKey;
		return this;
	}

	public BuildCoinbaseTransactionForm lockingAddress(Address lockingAddress) {
		this.lockingAddress = lockingAddress;
		return this;
	}

	// GETTERS & SETTERS // -----------------------------------------

	public TransactionScriptsType getScriptsType() {
		return scriptsType;
	}

	public void setScriptsType(TransactionScriptsType scriptsType) {
		this.scriptsType = scriptsType;
	}

	public int getBlockHeight() {
		return blockHeight;
	}

	public void setBlockHeight(int blockHeight) {
		this.blockHeight = blockHeight;
	}

	public String getScriptSigText() {
		return scriptSigText;
	}

	public void setScriptSigText(String scriptSigText) {
		this.scriptSigText = scriptSigText;
	}

	public BigInteger getAmount() {
		return amount;
	}

	public void setAmount(BigInteger amount) {
		this.amount = amount;
	}

	public PublicKey getLockingPublicKey() {
		return lockingPublicKey;
	}

	public void setLockingPublicKey(PublicKey lockingPublicKey) {
		this.lockingPublicKey = lockingPublicKey;
	}

	public Address getLockingAddress() {
		return lockingAddress;
	}

	public void setLockingAddress(Address lockingAddress) {
		this.lockingAddress = lockingAddress;
	}
}
