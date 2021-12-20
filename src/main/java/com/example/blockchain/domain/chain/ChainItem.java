package com.example.blockchain.domain.chain;

import com.example.blockchain.domain.block.Block;

public class ChainItem {

	private Block block;
	private int height;
	private double chainWork;

	public ChainItem(Block block, int height, double chainWork) {
		this.block = block;
		this.height = height;
		this.chainWork = chainWork;
	}

	public Block getBlock() {
		return block;
	}

	public int getHeight() {
		return height;
	}

	public double getChainWork() {
		return chainWork;
	}
}
