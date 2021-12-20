package com.example.blockchain.domain.block.builder;

import java.math.BigInteger;
import java.util.Date;

import com.example.blockchain.difficulty.Target;
import com.example.blockchain.domain.block.Block;
import com.example.blockchain.domain.transaction.Transaction;

public class BuildBlockForm {

	private Block previousBlock;
	private Transaction[] transactions;
	private Date time;
	private BigInteger target;

	public BuildBlockForm() {

	}

	public BuildBlockForm(Block previousBlock, Transaction[] transactions, Date time) {
		this.previousBlock = previousBlock;
		this.transactions = transactions;
		this.time = time;
		this.target = Target.MAX_TARGET_VALUE;
	}

	public BuildBlockForm(Block previousBlock, Transaction[] transactions, Date time, BigInteger target) {
		this.previousBlock = previousBlock;
		this.transactions = transactions;
		this.time = time;
		this.target = target;
	}

	// Builder Methods // -------------------------------------------

	public BuildBlockForm previousBlock(Block previousBlock) {
		this.previousBlock = previousBlock;
		return this;
	}

	public BuildBlockForm transactions(Transaction[] transactions) {
		this.transactions = transactions;
		return this;
	}

	public BuildBlockForm time(Date time) {
		this.time = time;
		return this;
	}

	public BuildBlockForm target(BigInteger target) {
		this.target = target;
		return this;
	}

	// GETTERS & SETTERS // -----------------------------------------

	public Block getPreviousBlock() {
		return previousBlock;
	}

	public void setPreviousBlock(Block previousBlock) {
		this.previousBlock = previousBlock;
	}

	public Transaction[] getTransactions() {
		return transactions;
	}

	public void setTransactions(Transaction[] transactions) {
		this.transactions = transactions;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public BigInteger getTarget() {
		return target;
	}

	public void setTarget(BigInteger target) {
		this.target = target;
	}
}
