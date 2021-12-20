package com.example.blockchain.domain.block.builder;

public class BlockSerializeResult {

	private byte[] data;
	private int nextIndex;

	public BlockSerializeResult(byte[] data, int nextIndex) {
		this.data = data;
		this.nextIndex = nextIndex;
	}

	// GETTERS & SETTERS // -----------------------------------------

	public byte[] getData() {
		return data;
	}

	public int getNextIndex() {
		return nextIndex;
	}
}
