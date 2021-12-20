package com.example.blockchain.domain.transaction;

public class TransactionInput {

	public static final long MAX_SEQUENCE_VALUE = Long.parseLong("FFFFFFFF", 16);

	private Transaction transaction;
	private int outputIndex; // TODO Limit the number of outputs to this number
	private byte[] scriptSig;
	private long sequence;

	public TransactionInput() {

	}

	public TransactionInput(Transaction transaction, int outputIndex, byte[] scriptSig, long sequence) {
		this.transaction = transaction;
		this.outputIndex = outputIndex;
		this.scriptSig = scriptSig;
		this.sequence = sequence;
	}

	// GETTERS & SETTERS // -----------------------------------------

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public int getOutputIndex() {
		return outputIndex;
	}

	public void setOutputIndex(int outputIndex) {
		this.outputIndex = outputIndex;
	}

	public byte[] getScriptSig() {
		return scriptSig;
	}

	public void setScriptSig(byte[] scriptSig) {
		this.scriptSig = scriptSig;
	}

	public long getSequence() {
		return sequence;
	}

	public void setSequence(long sequence) {
		this.sequence = sequence;
	}
}
