package com.example.blockchain.dao;

import java.util.List;

import com.example.blockchain.domain.block.Block;
import com.example.blockchain.domain.chain.ChainItem;
import com.example.blockchain.domain.transaction.Transaction;

public interface BlockDao {

	/**
	 * Gets the height.
	 * 
	 */
	int getHeight();

	/**
	 * Gets the tips.
	 * 
	 */
	List<ChainItem> getTips(int minHeight, int minHeightWithTransactions);

	/**
	 * Finds the block with the given hash.
	 * 
	 */
	Block findBlock(byte[] blockHash, boolean includeTransactions);

	/**
	 * Finds the chain item with the given hash.
	 * 
	 */
	ChainItem findChainItem(byte[] blockHash, boolean includeTransactions);

	/**
	 * Loads the data of the given block.
	 * 
	 */
	void load(Block block, boolean includeTransactions);

	/**
	 * Gets the active chain item at the given height.
	 * 
	 */
	ChainItem getActiveChainItem(int height);

	/**
	 * Finds the transactions of the given block.
	 * 
	 */
	Transaction[] findBlockTransactions(Block block);

	/**
	 * Saves the given new block, removes the previous block from the tips and
	 * updates the height if required.
	 * 
	 */
	void saveNewChainItem(ChainItem newChainItem, ChainItem previousChainItem, boolean updateHeight);
}
