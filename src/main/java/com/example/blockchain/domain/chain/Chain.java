package com.example.blockchain.domain.chain;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.blockchain.Callback;
import com.example.blockchain.dao.BlockHeightDao;
import com.example.blockchain.dao.TransactionDao;
import com.example.blockchain.dao.UnspentTransactionOutputDao;
import com.example.blockchain.domain.block.Block;
import com.example.blockchain.domain.block.GenesisBlock;
import com.example.blockchain.domain.transaction.pool.TransactionsPool;
import com.example.blockchain.domain.transaction.utxo.TransactionOutputsObserver;
import com.example.blockchain.network.NetworkInterface;
import com.example.blockchain.util.BigMath;
import com.example.blockchain.util.ConvertUtil;

public class Chain {

	private static final Logger log = LoggerFactory.getLogger(Chain.class);

	public static final long DURATION_BETWEEN_BLOCKS = 1000 * 60 * 10; // 10 minutes
	public static final BigInteger MAX_NUMBER_OF_HASHES = new BigInteger("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16);

	private static Chain INSTANCE;
	private static final ChainItem genesisChainItem = buildGenesisBlockChainItem();

	private int maxHeightOffsetOfLoadedBlocks = 5000;
	private int maxHeightOffsetOfLoadedBlocksWithTransactions = 20;

	List<ChainItem> blocks;
	List<ChainItem> tips;
	ChainItem activeTip;

	private ChainSynchronizer chainSynchronizer;
	private ChainTransactionsMaintainer chainTransactionsMaintainer;
	private NetworkInterface networkInterface;
	private BlockHeightDao blockHeightDao;

	private Set<BlockAddedSubscriber> subscribers = new HashSet<>();
	private Set<TransactionOutputsObserver> transactionOutputsObservers = new HashSet<>();

	Chain(TransactionsPool transactionsPool, NetworkInterface networkInterface, TransactionDao transactionDao,
			UnspentTransactionOutputDao unspentTransactionOutputDao, BlockHeightDao blockHeightDao) {

		this.networkInterface = networkInterface;
		this.blockHeightDao = blockHeightDao;

		chainSynchronizer = new ChainSynchronizer(networkInterface, true);
		chainTransactionsMaintainer = new ChainTransactionsMaintainer(transactionOutputsObservers, transactionsPool, transactionDao,
				unspentTransactionOutputDao);
		blocks = new LinkedList<ChainItem>();
		tips = new LinkedList<ChainItem>();
	}

	/**
	 * Initializes the only Chain instance.
	 * 
	 */
	public static Chain initializeInstance(TransactionsPool transactionsPool, NetworkInterface networkInterface, TransactionDao transactionDao,
			UnspentTransactionOutputDao unspentTransactionOutputDao, BlockHeightDao blockHeightDao) {

		INSTANCE = new Chain(transactionsPool, networkInterface, transactionDao, unspentTransactionOutputDao, blockHeightDao);
		return INSTANCE;
	}

	/**
	 * Returns the only chain instance.
	 * 
	 */
	public static Chain getInstance() {
		if (INSTANCE == null) {
			throw new RuntimeException("Instance not initialized");
		}
		return INSTANCE;
	}

	/**
	 * Builds the chain item of the genesis block.
	 * 
	 */
	private static ChainItem buildGenesisBlockChainItem() {
		Block genesisBlock = GenesisBlock.getInstance();
		double blockWork = calculateBlockWork(genesisBlock);
		ChainItem chainItem = new ChainItem(genesisBlock, 0, blockWork);
		return chainItem;
	}

	/**
	 * Calculates the block work.
	 * 
	 */
	private static double calculateBlockWork(Block block) {
		// FIXME Re-confirm the accuracy of the result
		BigInteger expectedHashesToMine = calculateExpectedHashesToMine(block.getBits().toTargetValue());
		double blockWork = log2(expectedHashesToMine);
		return roundToThreeDecimalPlaces(blockWork);
	}

	/**
	 * Calculates the chainwork for the given block.
	 * 
	 */
	private static BigInteger calculateExpectedHashesToMine(BigInteger targetValue) {
		BigInteger expectedHashesToMine = MAX_NUMBER_OF_HASHES.divide(targetValue.add(BigInteger.ONE));
		return expectedHashesToMine;
	}

	/**
	 * Calculates log with base 2.
	 * 
	 */
	private static double log2(BigInteger value) {
		return BigMath.logBigInteger(value) / Math.log(2.0);
	}

	/**
	 * Rounds the given number to two places.
	 * 
	 */
	private static double roundToThreeDecimalPlaces(double num) {
		return Math.round(num * 1000.0) / 1000.0;
	}

	/**
	 * Subscribes to the event of adding a block.
	 * 
	 */
	public void subscribe(BlockAddedSubscriber subscriber) {
		subscribers.add(subscriber);
	}

	/**
	 * Registers the given TransactionOutputsObserver.
	 * 
	 */
	public void register(TransactionOutputsObserver observer) {
		transactionOutputsObservers.add(observer);
	}

	/**
	 * Unregisters the given TransactionOutputsObserver.
	 * 
	 */
	public void unregister(TransactionOutputsObserver observer) {
		transactionOutputsObservers.remove(observer);
	}

	/**
	 * Initializes the chain.
	 * 
	 */
	public synchronized void initialize(Callback<ChainSynchronizationResult> callback) {
		chainSynchronizer.initialize(INSTANCE);
		loadChain();
		chainSynchronizer.synchChainWithNetwork(callback);
	}

	/**
	 * Loads the chain from the database.
	 * 
	 */
	private void loadChain() {
		blocks.clear();
		tips.clear();

		int height = Block.blockDao.getHeight();
		if (height == 0) {
			blocks.add(genesisChainItem);
			tips.add(genesisChainItem);
			activeTip = genesisChainItem;
		} else {
			int minHeight = height - maxHeightOffsetOfLoadedBlocks + 1;
			if (minHeight < 0) {
				minHeight = 0;
			}
			int minHeightWithTransactions = height - maxHeightOffsetOfLoadedBlocksWithTransactions + 1;
			if (minHeightWithTransactions < 0) {
				minHeightWithTransactions = 0;
			}
			List<ChainItem> tipsFromDatabase = Block.blockDao.getTips(minHeight, minHeightWithTransactions);
			this.tips.addAll(tipsFromDatabase);
			this.blocks.addAll(tipsFromDatabase);
			for (ChainItem tip : this.tips) {
				ChainItem lastAddedItem = tip;
				while (!lastAddedItem.getBlock().isGenesis() && lastAddedItem.getHeight() > minHeight) {
					ChainItem chainItem = findItemInBlocksList(lastAddedItem.getBlock().getPreviousBlock());
					if (chainItem != null) { // If item is already in the list
						break; // No need to retrieve the previous blocks since they're already added
					} else {
						if (lastAddedItem.getHeight() == 1) { // If 'lastAddedItem' precedes the genesis block
							chainItem = genesisChainItem;
						} else {
							chainItem = Block.blockDao.findChainItem(lastAddedItem.getBlock().getPreviousBlock().getHash(),
									lastAddedItem.getHeight() > minHeightWithTransactions ? true : false);
						}
						blocks.add(chainItem);
					}
					lastAddedItem = chainItem;
				}
			}
			activeTip = findHighestTip();
		}
	}

	/**
	 * Prints the chain.
	 * 
	 */
	public void printChain() {
		System.out.println("Number of blocks: " + this.blocks.size());

		ChainItem ci = getHighestTip();
		String blockHash = ConvertUtil.byteArrayToHexString(ci.getBlock().getHash());
		System.out.println(ci.getHeight() + " - " + blockHash + " - " + ci.getBlock().serialize(true).length + " - " + ci.getBlock().getTransactions().length
				+ " - " + ci.getBlock().getTime());
		while (!Arrays.equals(ci.getBlock().getHash(), GenesisBlock.getInstance().getHash())) {
			ci = findItemInBlocksList(ci.getBlock().getPreviousBlock().getHash());
			blockHash = ConvertUtil.byteArrayToHexString(ci.getBlock().getHash());
			System.out.println(ci.getHeight() + " - " + blockHash + " - " + ci.getBlock().serialize(true).length + " - "
					+ ci.getBlock().getTransactions().length + " - " + ci.getBlock().getTime());
		}
	}

	/**
	 * Finds the tip with the highest chainwork in the tips list.
	 * 
	 */
	private ChainItem findHighestTip() {
		ChainItem highestTip = tips.get(0);
		for (ChainItem tip : tips) {
			if (tip.getChainWork() > highestTip.getChainWork()) {
				highestTip = tip;
			}
		}
		return highestTip;
	}

	public boolean validate() {
		// TODO Implement
		throw new NotImplementedException("");
	}

	/**
	 * Adds the given block to the chain.
	 * 
	 */
	public synchronized boolean addBlock(Block block) {
		return addBlock(block, false);
	}

	/**
	 * Adds the given block to the chain.
	 * 
	 */
	public synchronized boolean addBlock(Block block, boolean minedLocally) {
		// TODO Validate the block

		if (Block.blockDao.findChainItem(block.getHash(), false) != null) {
			return false;
		}

		ChainItem initialHighestTip = getHighestTip();
		ChainItem previousChainItem = findItemInBlocksList(block.getPreviousBlock());
		if (previousChainItem == null) {
			previousChainItem = Block.blockDao.findChainItem(block.getPreviousBlock().getHash(), false);
			if (previousChainItem == null) {
				new ChainSynchronizer(INSTANCE, networkInterface, false).synchBlockAndPrevious(block, null);
			}
		}
		int heightOfNewBlock = previousChainItem.getHeight() + 1;
		double chainWorkOfPreviousBlock = previousChainItem.getChainWork();
		double chainWorkOfNewBlock = chainWorkOfPreviousBlock + calculateBlockWork(block);
		ChainItem newChainItem = new ChainItem(block, heightOfNewBlock, chainWorkOfNewBlock);
		blocks.add(newChainItem);
		tips.add(newChainItem);
		tips.remove(previousChainItem);
		boolean newBlockIsActiveTip = false;
		if (newChainItem.getChainWork() > initialHighestTip.getChainWork()) {
			activeTip = newChainItem;
			newBlockIsActiveTip = true;
		}

		Block.blockDao.saveNewChainItem(newChainItem, previousChainItem, newBlockIsActiveTip);
		blockHeightDao.saveBlockHeight(block, heightOfNewBlock);

		ChainItem finalHighestTip = getHighestTip();
		boolean activeBranchWasChanged = !finalHighestTip.getBlock().getPreviousBlock().equals(initialHighestTip.getBlock());
		if (activeBranchWasChanged) {
			chainTransactionsMaintainer.maintainAfterSwitchingBranches(initialHighestTip.getBlock(), finalHighestTip.getBlock());
		} else {
			chainTransactionsMaintainer.maintainAfterAddingBlockToActiveBranch(block);
		}

		if (newBlockIsActiveTip) {
			notifySubscribers(block, heightOfNewBlock, chainWorkOfNewBlock, true, minedLocally);
		} else {
			notifySubscribers(block, heightOfNewBlock, chainWorkOfNewBlock, false, minedLocally);
		}

		cleanUpOldBlocks();

		log.info("Added block {}", ConvertUtil.byteArrayToHexString(block.getHash()));

		return true;
	}

	/**
	 * Notifies the subscribers of adding a block.
	 * 
	 */
	private void notifySubscribers(Block block, int height, double chainWork, boolean isActiveTip, boolean minedLocally) {
		for (BlockAddedSubscriber subscriber : subscribers) {
			subscriber.blockAdded(block, height, chainWork, isActiveTip, minedLocally);
		}
	}

	/**
	 * Finds the item in the list with the given block.
	 * 
	 */
	private ChainItem findItemInBlocksList(Block block) {
		return findItemInBlocksList(block.getHash());
	}

	/**
	 * Finds the item in the list with the given block hash.
	 * 
	 */
	private ChainItem findItemInBlocksList(byte[] blockHash) {
		for (ChainItem chainItem : blocks) {
			if (Arrays.equals(chainItem.getBlock().getHash(), blockHash)) {
				return chainItem;
			}
		}
		return null;
	}

	/**
	 * Returns the block with the given hash.
	 * 
	 */
	public boolean isBlockExists(byte[] blockHash) {
		return (getBlock(blockHash, false) != null);
	}

	/**
	 * Returns the block with the given hash.
	 * 
	 */
	public Block getBlock(byte[] blockHash, boolean includeTransactions) {
		return getChainItem(blockHash, includeTransactions).getBlock();
	}

	/**
	 * Returns the chain item with the given block.
	 * 
	 */
	public ChainItem getChainItem(Block block, boolean includeTransactions) {
		return getChainItem(block.getHash(), includeTransactions);
	}

	/**
	 * Returns the chain item with the given hash.
	 * 
	 */
	public ChainItem getChainItem(byte[] blockHash, boolean includeTransactions) {
		if (Arrays.equals(genesisChainItem.getBlock().getHash(), blockHash)) {
			return genesisChainItem;
		}
		ChainItem chainItem = findItemInBlocksList(blockHash);
		if (chainItem == null) {
			chainItem = Block.blockDao.findChainItem(blockHash, includeTransactions);
		}
		return chainItem;
	}

	/**
	 * Returns the chain items at the given height.
	 * 
	 */
	public List<Block> getBlocks(int height, boolean includeTransactions) {
		List<ChainItem> chainItems = getChainItems(height, includeTransactions);
		List<Block> blocks = new ArrayList<>(chainItems.size());
		for (ChainItem chainItem : chainItems) {
			blocks.add(chainItem.getBlock());
		}
		return blocks;
	}

	/**
	 * Returns the chain items at the given height.
	 * 
	 */
	public List<ChainItem> getChainItems(int height, boolean includeTransactions) {
		if (height == 0) {
			List<ChainItem> chainItems = new ArrayList<>(1);
			chainItems.add(genesisChainItem);
			return chainItems;
		} else {
			List<byte[]> blockHashes = blockHeightDao.findBlockHashesAtHeight(height);
			List<ChainItem> chainItems = new ArrayList<>(blockHashes.size());
			for (byte[] hash : blockHashes) {
				chainItems.add(getChainItem(hash, includeTransactions));
			}
			return chainItems;
		}
	}

	/**
	 * Cleans up old blocks which should not be cached in the chain.
	 * 
	 */
	private void cleanUpOldBlocks() {
		cleanUpOldBlocks(blocks);
		cleanUpOldBlocks(tips);
	}

	/**
	 * Cleans up old blocks which should not be cached in the given list.
	 * 
	 */
	private void cleanUpOldBlocks(List<ChainItem> list) {
		int currentHeight = getHeight();

		// Removes old blocks
		if (currentHeight > maxHeightOffsetOfLoadedBlocks) {
			int minimumHeight = currentHeight - maxHeightOffsetOfLoadedBlocks + 1;
			List<ChainItem> chainItemsToBeRemoved = new ArrayList<>();
			for (ChainItem chainItem : list) {
				if (chainItem.getHeight() < minimumHeight) {
					chainItemsToBeRemoved.add(chainItem);
				}
			}
			list.removeAll(chainItemsToBeRemoved);
		}

		// Nullifies the transactions of the relatively old blocks
		if (currentHeight > maxHeightOffsetOfLoadedBlocks) {
			int minimumHeightWithTransactions = currentHeight - maxHeightOffsetOfLoadedBlocksWithTransactions + 1;
			for (ChainItem chainItem : list) {
				if (chainItem.getHeight() < minimumHeightWithTransactions) {
					chainItem.getBlock().nullifyTransactions();
				}
			}
		}
	}

	/**
	 * Returns the height of the chain.
	 * 
	 */
	public int getHeight() {
		ChainItem highestTip = getHighestTip();
		return highestTip.getHeight();
	}

	/**
	 * Gets the tip with the highest chainwork.
	 * 
	 */
	private ChainItem getHighestTip() {
		return activeTip;
	}

	/**
	 * Returns the active block at the given height.
	 * 
	 */
	public Block getActiveBlock(int height) {
		// Validates the given height
		int currentHeight = getHighestTip().getHeight();
		if (height > currentHeight) {
			throw new IllegalArgumentException("Invalid height");
		}

		// Returns the genesis block if given height is 0
		if (height == 0) {
			return GenesisBlock.getInstance();
		}

		// Iterates through the blocks list and returns the block if found
		for (ChainItem chainItem : blocks) {
			if (chainItem.getHeight() == height) {
				return chainItem.getBlock();
			}
		}

		// Returns the block from the DAO
		return Block.blockDao.getActiveChainItem(height).getBlock();
	}

	/**
	 * Returns the last active block.
	 * 
	 */
	public Block getLastActiveBlock() {
		ChainItem highestTip = getHighestTip();
		return highestTip.getBlock();
	}

	/**
	 * Returns the last active chain item.
	 * 
	 */
	public ChainItem getLastActiveChainItem() {
		ChainItem highestTip = getHighestTip();
		return highestTip;
	}

	/**
	 * Returns the last n active blocks as specified by the parameter 'count'. If
	 * less than what's requested is available, throws an exception and attaches the
	 * available ones.
	 * 
	 */
	public List<Block> getLastActiveBlocks(int count, boolean includeTransactions) {
		if (count < 1) {
			throw new IllegalArgumentException("The count cannot be less than one");
		}

		ChainItem chainItem = getHighestTip();
		int currentHeight = chainItem.getHeight();
		if (count > currentHeight + 1) {
			throw new TooManyBlocksRequestedException(currentHeight + 1);
		}

		List<Block> lastActiveBlocks = new ArrayList<>(count);
		lastActiveBlocks.add(chainItem.getBlock());
		for (int i = 0; i < count - 1; i++) {
			Block lastBlock = chainItem.getBlock();
			chainItem = getChainItem(lastBlock.getPreviousBlock(), includeTransactions);
			lastActiveBlocks.add(chainItem.getBlock());
		}
		return lastActiveBlocks;
	}

	/**
	 * Returns the Genesis chain item.
	 * 
	 */
	public ChainItem getGenesisChainItem() {
		return genesisChainItem;
	}
}
