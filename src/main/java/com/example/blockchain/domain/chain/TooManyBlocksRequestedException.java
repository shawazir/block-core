package com.example.blockchain.domain.chain;

public class TooManyBlocksRequestedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private int availableBlocksCount;

	public TooManyBlocksRequestedException(int availableBlocksCount) {
		this.availableBlocksCount = availableBlocksCount;
	}

	public int getAvailableBlocksCount() {
		return availableBlocksCount;
	}
}
