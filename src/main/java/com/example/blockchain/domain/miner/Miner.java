package com.example.blockchain.domain.miner;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.blockchain.difficulty.TargetCalculator;
import com.example.blockchain.domain.account.Account;
import com.example.blockchain.domain.block.Block;
import com.example.blockchain.domain.block.builder.BlockBuilderV1;
import com.example.blockchain.domain.block.builder.BuildBlockForm;
import com.example.blockchain.domain.chain.BlockAddedSubscriber;
import com.example.blockchain.domain.chain.Chain;
import com.example.blockchain.domain.chain.ChainItem;
import com.example.blockchain.domain.clock.NetworkClock;
import com.example.blockchain.domain.transaction.Transaction;
import com.example.blockchain.domain.transaction.builder.BuildCoinbaseTransactionForm;
import com.example.blockchain.domain.transaction.pool.TransactionsPool;
import com.example.blockchain.domain.transaction.script.TransactionScriptsType;
import com.example.blockchain.network.NetworkInterface;
import com.example.blockchain.puzzle.PuzzleSolvedSubscriber;
import com.example.blockchain.puzzle.PuzzleSolver;
import com.example.blockchain.puzzle.PuzzleSolverFactory;
import com.example.blockchain.util.BlockSubsidyUtil;
import com.example.blockchain.util.ConvertUtil;

public class Miner implements PuzzleSolvedSubscriber, BlockAddedSubscriber {

	private static final Logger log = LoggerFactory.getLogger(Miner.class);

	private Chain chain;
	private TargetCalculator targetCalculator;
	private TransactionsPool transactionsPool;
	private NetworkClock networkClock;
	private NetworkInterface networkInterface;
	private Account rewardAccount;

	private PuzzleSolver puzzleSolver;

	public Miner(TargetCalculator targetCalculator, TransactionsPool transactionsPool, NetworkClock networkClock, NetworkInterface networkInterface,
			Account rewardAccount) {

		chain = Chain.getInstance();
		this.targetCalculator = targetCalculator;
		this.transactionsPool = transactionsPool;
		this.networkClock = networkClock;
		this.networkInterface = networkInterface;
		this.rewardAccount = rewardAccount;

		chain.subscribe(this);
	}

	/**
	 * Starts the miner.
	 * 
	 */
	public void start() {
		log.info("Started Miner");
		startMining();
	}

	/**
	 * Starts the mining process.
	 * 
	 */
	private void startMining() {
		// Retrieves the last chain item to start
		ChainItem lastChainItem = chain.getLastActiveChainItem();
		startMiningNewBlock(lastChainItem);

		// Checks if a new block was added to the chain while preparing the
		// candidate block
		boolean newBlockAdded = !Arrays.equals(chain.getLastActiveChainItem().getBlock().getHash(), lastChainItem.getBlock().getHash());
		while (newBlockAdded) {
			stopMining();
			lastChainItem = chain.getLastActiveChainItem();
			startMiningNewBlock(lastChainItem);
		}
	}

	/**
	 * Starts the mining process.
	 * 
	 */
	private void startMiningNewBlock(ChainItem lastChainItem) {
		int newHeight = lastChainItem.getHeight() + 1;
		BigInteger subsidy = BlockSubsidyUtil.calculateSubsidyInSatoshis(newHeight);
		Block candidateBlock = createCandidateBlock(lastChainItem.getBlock(), newHeight, null, subsidy, rewardAccount.getKeyPair().getPublic());
		if (puzzleSolver != null) {
			puzzleSolver.stop();
		}
		puzzleSolver = PuzzleSolverFactory.createPuzzleSolver(targetCalculator);
		puzzleSolver.subscribe(this);
		puzzleSolver.start(candidateBlock);
		log.info("Started mining block ({})", newHeight);
	}

	/**
	 * Creates a new candidate block.
	 * 
	 */
	private Block createCandidateBlock(Block lastBlock, int height, String scriptSigText, BigInteger blockSubsidy,
			PublicKey lockingPublicKeyOfCoinbaseTransaction) {
		// Creates the coinbase transaction
		BuildCoinbaseTransactionForm buildCoinbaseTransactionForm = new BuildCoinbaseTransactionForm(TransactionScriptsType.PAY_TO_PUBLIC_KEY, height,
				scriptSigText, blockSubsidy, lockingPublicKeyOfCoinbaseTransaction, null);
		Transaction coinbaseTx = Transaction.createCoinbaseTransaction(buildCoinbaseTransactionForm);

		// Retrieves the transactions
		int maxVarIntSize = 3; // The number of expected transactions should not exceed the capacity of
								// two-byte VarInt
		int transactionsMaxSize = Block.DEFAULT_MAX_SIZE - BlockBuilderV1.BLOCK_HEADER_SIZE - coinbaseTx.getSize() - maxVarIntSize;
		List<Transaction> transactionsList = transactionsPool.getCandidateTransactions(transactionsMaxSize);
		Transaction[] transactions = convertTransactionsListToTransactionsArray(coinbaseTx, transactionsList);

		// Calculates the fees; if any, adds to the subsidy
		BigInteger transactionFees = calculateTransactionFees(transactions);
		if (transactionFees.compareTo(BigInteger.ZERO) == 1) { // Larger than one
			coinbaseTx.getOutputs().get(0).setAmount(blockSubsidy.add(transactionFees));
		}

		// Builds the candidate block
		BuildBlockForm buildBlockForm = new BuildBlockForm(lastBlock, transactions, networkClock.getNetworkTime());
		Block candidateBlock = Block.createCandidateBlock(buildBlockForm);
		return candidateBlock;
	}

	/**
	 * Converts the given transactions list to an array that also includes the
	 * coinbase transaction.
	 * 
	 */
	private Transaction[] convertTransactionsListToTransactionsArray(Transaction coinbaseTx, List<Transaction> transactionsList) {
		Transaction[] transactionsArray = new Transaction[transactionsList.size() + 1];
		transactionsArray[0] = coinbaseTx;
		for (int i = 0; i < transactionsList.size(); i++) {
			transactionsArray[i + 1] = transactionsList.get(i);
		}
		return transactionsArray;
	}

	/**
	 * Calculates the transaction fees for the given transactions.
	 * 
	 */
	private BigInteger calculateTransactionFees(Transaction[] transactions) {
		BigInteger fees = BigInteger.valueOf(0);
		for (Transaction tx : transactions) {
			fees = fees.add(tx.getFees());
		}
		return fees;
	}

	/**
	 * Stops the miner.
	 * 
	 */
	public void stop() {
		stopMining();
		log.info("Stopped Miner");
	}

	/**
	 * Stops the mining process.
	 * 
	 */
	private void stopMining() {
		if (puzzleSolver.isRunning()) {
			puzzleSolver.stop();
		}
	}

	@Override
	public void puzzleSolved(Block block) {
		log.info("Solved block (Hash: {})", ConvertUtil.byteArrayToHexString(block.getHash()));
		networkInterface.relayBlock(block);
		chain.addBlock(block, true);
		startMining();
	}

	@Override
	public void blockAdded(Block block, int height, double chainWork, boolean isActiveTip, boolean minedLocally) {
		if (!minedLocally) {
			if (isActiveTip) {
				stopMining();
				startMining();
			}
		}
	}
}
