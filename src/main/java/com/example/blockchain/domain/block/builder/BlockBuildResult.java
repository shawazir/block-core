package com.example.blockchain.domain.block.builder;

import com.example.blockchain.domain.block.Block;

public class BlockBuildResult {

	private Block block;
	private int nextIndex;

	public BlockBuildResult(Block block, int nextIndex) {
		this.block = block;
		this.nextIndex = nextIndex;
	}

	// GETTERS & SETTERS // -----------------------------------------

	public Block getBlock() {
		return block;
	}

	public int getNextIndex() {
		return nextIndex;
	}
}
