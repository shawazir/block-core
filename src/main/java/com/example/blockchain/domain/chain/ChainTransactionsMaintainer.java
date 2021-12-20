package com.example.blockchain.domain.chain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.example.blockchain.DualReturn;
import com.example.blockchain.dao.TransactionDao;
import com.example.blockchain.dao.UnspentTransactionOutputDao;
import com.example.blockchain.domain.block.Block;
import com.example.blockchain.domain.transaction.Transaction;
import com.example.blockchain.domain.transaction.pool.TransactionsPool;
import com.example.blockchain.domain.transaction.utxo.TransactionOutputsObserver;
import com.example.blockchain.domain.transaction.utxo.UnspentTransactionOutput;

/**
 * Maintains the correctness of the transactions (and UTXOs) of the chain after
 * adding a block or switching a branch.
 *
 */
public class ChainTransactionsMaintainer {

	private Set<TransactionOutputsObserver> transactionOutputsObservers;

	private TransactionsPool transactionsPool;

	private TransactionDao transactionDao;

	private UnspentTransactionOutputDao unspentTransactionOutputDao;

	public ChainTransactionsMaintainer(Set<TransactionOutputsObserver> transactionOutputsObservers, TransactionsPool transactionsPool,
			TransactionDao transactionDao, UnspentTransactionOutputDao unspentTransactionOutputDao) {

		this.transactionOutputsObservers = transactionOutputsObservers;
		this.transactionsPool = transactionsPool;
		this.transactionDao = transactionDao;
		this.unspentTransactionOutputDao = unspentTransactionOutputDao;
	}

	public void maintainAfterAddingBlockToActiveBranch(Block newBlock) {
		// Procedure:
		// Remove the TXs from the pool
		// Add the TXs to the storage
		// Save the produced UTXOs to the storage
		// Delete the consumed UTXOs from the storage

		Transaction[] transactions = newBlock.getTransactions();
		DualReturn<List<UnspentTransactionOutput>, List<UnspentTransactionOutput>> consumedAndProducedOutputs = getConsumedAndProducedTransactionOutputs(
				Arrays.asList(transactions));
		transactionsPool.removeTransactions(transactions);
		transactionDao.save(transactions);
		List<UnspentTransactionOutput> consumedOutputs = consumedAndProducedOutputs.getValue1(); // To be deleted from UTXOs
		List<UnspentTransactionOutput> producedOutputs = consumedAndProducedOutputs.getValue2(); // To be added to UTXOs
		unspentTransactionOutputDao.delete(consumedOutputs);
		unspentTransactionOutputDao.save(producedOutputs);
		notifyTransactionOutputsObserver(producedOutputs, consumedOutputs);
	}

	public void maintainAfterSwitchingBranches(Block oldTip, Block newTip) {
		// Procedure:
		// Old Tip: Add the TXs to the pool
		// Old Tip: Delete the TXs from the storage
		// Old Tip: Delete the produced UTXOs from the storage
		// Old Tip: Restore the consumed UTXOs to the storage
		// New Tip: Remove the TXs from the pool
		// New Tip: Add the TXs to the storage
		// New Tip: Save the produced UTXOs to the storage
		// New Tip: Delete the consumed UTXOs from the storage

		// Finds the oldest non-shared parents of the old tip and new tip
		DualReturn<Block, Block> oldestNonSharedParents = findOldestNonSharedParents(oldTip, newTip);
		Block oldestParentOfOldBranch = oldestNonSharedParents.getValue1();
		Block oldestParentOfNewBranch = oldestNonSharedParents.getValue2();

		// Collects the transactions of the old branch and new branch which are not
		// shared between the two
		List<Transaction> transactionsOfOldBranch = collectTransactionsOfChainSubset(oldestParentOfOldBranch, oldTip);
		List<Transaction> transactionsOfNewBranch = collectTransactionsOfChainSubset(oldestParentOfNewBranch, newTip);
		removeSharedTransactions(transactionsOfOldBranch, transactionsOfNewBranch);

		// Collects the UTXOs that are to be added and those to be deleted
		DualReturn<List<UnspentTransactionOutput>, List<UnspentTransactionOutput>> oldConsumedAndProducedOutputs = getConsumedAndProducedTransactionOutputs(
				transactionsOfOldBranch);
		List<UnspentTransactionOutput> oldConsumedOutputs = oldConsumedAndProducedOutputs.getValue1(); // To be added to UTXOs
		List<UnspentTransactionOutput> oldProducedOutputs = oldConsumedAndProducedOutputs.getValue2(); // To be deleted from UTXOs
		DualReturn<List<UnspentTransactionOutput>, List<UnspentTransactionOutput>> newConsumedAndProducedOutputs = getConsumedAndProducedTransactionOutputs(
				transactionsOfNewBranch);
		List<UnspentTransactionOutput> newConsumedOutputs = newConsumedAndProducedOutputs.getValue1(); // To be deleted from UTXOs
		List<UnspentTransactionOutput> newProducedOutputs = newConsumedAndProducedOutputs.getValue2(); // To be added to UTXOs

		// Removes the outputs which are supposed to be added then deleted and those
		// which are supposed to be deleted then added because doing two operations that
		// cancel each other is unnecessary
		removeSharedOutputs(oldConsumedOutputs, newConsumedOutputs);
		removeSharedOutputs(oldProducedOutputs, newProducedOutputs);

		// Constructs the list of outputs which should be added
		Set<UnspentTransactionOutput> outputsToSave = new HashSet<>();
		outputsToSave.addAll(oldConsumedOutputs);
		outputsToSave.addAll(newProducedOutputs);

		// Constructs the list of outputs which should be deleted
		Set<UnspentTransactionOutput> outputsToDelete = new HashSet<>();
		outputsToDelete.addAll(oldProducedOutputs);
		outputsToDelete.addAll(newConsumedOutputs);

		transactionsPool.addTransactions(transactionsOfOldBranch.toArray(new Transaction[transactionsOfOldBranch.size()]));
		transactionDao.delete(transactionsOfOldBranch.toArray(new Transaction[transactionsOfOldBranch.size()]));
		transactionsPool.removeTransactions(transactionsOfNewBranch.toArray(new Transaction[transactionsOfNewBranch.size()]));
		transactionDao.save(transactionsOfNewBranch.toArray(new Transaction[transactionsOfNewBranch.size()]));
		List<UnspentTransactionOutput> outputsToSaveList = new ArrayList<>();
		outputsToSaveList.addAll(outputsToSave);
		List<UnspentTransactionOutput> outputsToDeleteList = new ArrayList<>();
		outputsToDeleteList.addAll(outputsToDelete);
		unspentTransactionOutputDao.save(outputsToSaveList);
		unspentTransactionOutputDao.delete(outputsToDeleteList);
		notifyTransactionOutputsObserver(outputsToSaveList, outputsToDeleteList);
	}

	/**
	 * Collects the transactions of the branch subset which starts with the given
	 * 'firstBlock' and ends with the given 'lastBlock'.
	 * 
	 */
	private List<Transaction> collectTransactionsOfChainSubset(Block firstBlock, Block lastBlock) {
		Block block = lastBlock;
		List<Transaction> transactions = new LinkedList<>();
		while (true) {
			transactions.addAll(Arrays.asList(block.getTransactions())); // FIXME Potential performance degradation
			if (block == firstBlock) {
				break;
			}
			block = block.getPreviousBlock();
		}
		return transactions;
	}

	/**
	 * Removes the transactions that are shared between the two given lists.
	 * 
	 */
	private void removeSharedTransactions(List<Transaction> list1, List<Transaction> list2) {
		Iterator<Transaction> list1Iterator = list1.iterator();
		Iterator<Transaction> list2Iterator;
		Transaction list1Tx = null;
		Transaction list2Tx = null;
		while (list1Iterator.hasNext()) {
			list1Tx = list1Iterator.next();
			list2Iterator = list2.iterator();
			while (list2Iterator.hasNext()) {
				list2Tx = list2Iterator.next();
				if (Arrays.equals(list1Tx.getId(), list2Tx.getId())) {
					list1Iterator.remove();
					list2Iterator.remove();
					break;
				}
			}
		}
	}

	/**
	 * Removes the outputs that are shared between the two given lists.
	 * 
	 */
	private void removeSharedOutputs(List<UnspentTransactionOutput> list1, List<UnspentTransactionOutput> list2) {
		Iterator<UnspentTransactionOutput> list1Iterator = list1.iterator();
		Iterator<UnspentTransactionOutput> list2Iterator;
		UnspentTransactionOutput list1Output = null;
		UnspentTransactionOutput list2Output = null;
		while (list1Iterator.hasNext()) {
			list1Output = list1Iterator.next();
			list2Iterator = list2.iterator();
			while (list2Iterator.hasNext()) {
				list2Output = list2Iterator.next();
				if (list1Output.getTransaction().equals(list2Output.getTransaction()) && list1Output.getOutputIndex() == list2Output.getOutputIndex()) {
					list1Iterator.remove();
					list2Iterator.remove();
					break;
				}
			}
		}
	}

	/**
	 * Finds the oldest non-shared parent blocks of the two given tips.
	 * 
	 */
	private DualReturn<Block, Block> findOldestNonSharedParents(Block oldTip, Block newTip) {
		if (findIfBlocksHaveSamePreviousBlock(oldTip, newTip)) {
			return new DualReturn<>(oldTip, newTip);
		}

		List<Block> blocksOfOldBranch = new ArrayList<>();
		blocksOfOldBranch.add(oldTip);
		List<Block> blocksOfNewBranch = new ArrayList<>();
		blocksOfNewBranch.add(newTip);
		boolean checkOldBranch = true;
		while (true) {
			if (checkOldBranch) {
				oldTip = oldTip.getPreviousBlock();
				blocksOfOldBranch.add(oldTip);
				for (Block block : blocksOfNewBranch) {
					if (findIfBlocksHaveSamePreviousBlock(oldTip, block)) {
						return new DualReturn<>(oldTip, block);
					}
				}
			} else {
				newTip = newTip.getPreviousBlock();
				blocksOfNewBranch.add(newTip);
				for (Block block : blocksOfOldBranch) {
					if (findIfBlocksHaveSamePreviousBlock(newTip, block)) {
						return new DualReturn<>(block, newTip);
					}
				}
			}
			checkOldBranch = !checkOldBranch;
		}
	}

	/**
	 * Finds if the given blocks have the same previous block.
	 * 
	 */
	private boolean findIfBlocksHaveSamePreviousBlock(Block block1, Block block2) {
		return Arrays.equals(block1.getPreviousBlock().getHash(), block2.getPreviousBlock().getHash());
	}

	/**
	 * Gets the consumed transaction outputs and the produced transaction outputs
	 * from the given transactions.
	 * 
	 */
	private DualReturn<List<UnspentTransactionOutput>, List<UnspentTransactionOutput>> getConsumedAndProducedTransactionOutputs(
			List<Transaction> transactions) {

		List<UnspentTransactionOutput> consumedTransactionOutputs = new ArrayList<>(transactions.size());
		List<UnspentTransactionOutput> producedTransactionOutputs = new ArrayList<>(transactions.size());
		for (Transaction tx : transactions) {
			consumedTransactionOutputs.addAll(Arrays.asList(tx.getConsumedOutputs()));
			producedTransactionOutputs.addAll(Arrays.asList(tx.getProducedOutputs()));
		}
		return new DualReturn<List<UnspentTransactionOutput>, List<UnspentTransactionOutput>>(consumedTransactionOutputs, producedTransactionOutputs);
	}

	private void notifyTransactionOutputsObserver(List<UnspentTransactionOutput> addedOutputs, List<UnspentTransactionOutput> deletedOutputs) {
		for (TransactionOutputsObserver observer : transactionOutputsObservers) {
			observer.outputsUpdated(addedOutputs, deletedOutputs);
		}
	}
}
