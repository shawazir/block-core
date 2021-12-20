package com.example.blockchain.domain.chain;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.blockchain.Callback;
import com.example.blockchain.domain.block.Block;
import com.example.blockchain.network.NetworkCallback;
import com.example.blockchain.network.NetworkInterface;
import com.example.blockchain.network.NodeAvailabilitySubscriber;
import com.example.blockchain.network.handler.GetResponse;

/**
 * Synchronizes the chain with the network.
 *
 */
public class ChainSynchronizer implements NodeAvailabilitySubscriber {

	private static final Logger log = LoggerFactory.getLogger(Chain.class);

	private static final int CHAIN_SYNCH_INTERVAL = 1000 * 60 * 60 * 6; // Six hours
	private static final int CHAIN_SYNCH_DELAY_AFTER_FAILURE = 1000 * 60 * 5; // Five minutes
	private static final int CHAIN_SYNCH_DELAY_IF_BUSY = 1000 * 60 * 5; // Five minutes
	private static final int MAX_ALLOWED_NUMBER_OF_FAILURES = 5; // Five minutes
	private static final int MIN_NUMBER_OF_NODES = 1;

	private Queue<BlockDownloadRequest> downloadQueue = new PriorityQueue<>();

	private Chain chain;
	private NetworkInterface networkInterface;
	private boolean busy = false;
	private boolean alwaysRunning; // True if the instance is supposed to keep running and check the height
									// is in synch frequently
	private Timer timer = new Timer();
	private boolean needsToUpdateWithEnoughNodes = false;

	public ChainSynchronizer(NetworkInterface networkInterface, boolean alwaysRunning) {
		this(null, networkInterface, alwaysRunning);
	}

	public ChainSynchronizer(Chain chain, NetworkInterface networkInterface, boolean alwaysRunning) {
		this.chain = chain;
		this.networkInterface = networkInterface;
		this.alwaysRunning = alwaysRunning;
	}

	public void initialize(Chain chain) {
		this.chain = chain;
		networkInterface.subscribeNodeAvailabilitySubscriber(this, MIN_NUMBER_OF_NODES);
	}

	public boolean isBusy() {
		return busy;
	}

	/**
	 * Synchs the chain with the network.
	 * 
	 */
	public void synchChainWithNetwork() {
		synchronized (this) {
			if (busy) {
				throw new RuntimeException("Synchronizer is busy");
			}
			this.busy = true;
		}

		synchChainWithNetworkInternal(null, new Callback<ChainSynchronizationResult>() {
			@Override
			public void onSuccess(ChainSynchronizationResult result) {

			}

			@Override
			public void onFailure(ChainSynchronizationResult result) {

			}
		});
	}

	/**
	 * Synchs the chain with the network.
	 * 
	 */
	public void synchChainWithNetwork(Integer networkHeight) {
		synchronized (this) {
			if (busy) {
				throw new RuntimeException("Synchronizer is busy");
			}
			this.busy = true;
		}

		synchChainWithNetworkInternal(networkHeight, new Callback<ChainSynchronizationResult>() {
			@Override
			public void onSuccess(ChainSynchronizationResult result) {

			}

			@Override
			public void onFailure(ChainSynchronizationResult result) {

			}
		});
	}

	/**
	 * Synchs the chain with the network.
	 * 
	 */
	public void synchChainWithNetwork(Callback<ChainSynchronizationResult> callback) {
		synchronized (this) {
			if (busy) {
				throw new RuntimeException("Synchronizer is busy");
			}
			this.busy = true;
		}

		synchChainWithNetworkInternal(null, callback);
	}

	/**
	 * Synchs the chain with the network.
	 * 
	 */
	public void synchChainWithNetwork(Integer networkHeight, Callback<ChainSynchronizationResult> callback) {
		synchronized (this) {
			if (busy) {
				throw new RuntimeException("Synchronizer is busy");
			}
			this.busy = true;
		}

		synchChainWithNetworkInternal(networkHeight, callback);
	}

	/**
	 * Synchs the chain with the network. For internal use; does not check the
	 * instance variable 'busy'.
	 * 
	 */
	private void synchChainWithNetworkInternal(Integer networkHeight, Callback<ChainSynchronizationResult> callback) {
		if (networkHeight == null) {
			getNetworkChainHeight(callback);
			return;
		}

		int currentChainHeight = chain.getHeight();
		if (currentChainHeight >= networkHeight.intValue()) {
			callback.onSuccess(ChainSynchronizationResult.SUCCESS);
			scheduleChainSynch(CHAIN_SYNCH_INTERVAL);
			this.busy = false;
			log.info("Chain synchronization completed successfully");
			return;
		} else {
			downloadBlocks(currentChainHeight, networkHeight, callback);
		}
	}

	/**
	 * Synchs the chain with this block and its previous block(s). This method is
	 * meant to be used by the chain when it tries to add a block but cannot because
	 * its previous block does not exist.
	 * 
	 */
	public void synchBlockAndPrevious(Block block, Callback<ChainSynchronizationResult> callback) {
		synchronized (this) {
			if (busy) {
				throw new RuntimeException("Synchronizer is busy");
			}
			this.busy = true;
		}

		if (callback == null) {
			callback = new Callback<ChainSynchronizationResult>() {
				@Override
				public void onSuccess(ChainSynchronizationResult result) {

				}

				@Override
				public void onFailure(ChainSynchronizationResult result) {

				}
			};
		}

		BlockDownloadRequest request;
		if (block.isTransactionsNull()) {
			request = new BlockDownloadRequest(-1, block.getHash(), null);
		} else {
			request = new BlockDownloadRequest(-1, block.getPreviousBlock().getHash(), new BlockDownloadRequest(-1, block.getHash(), null, block));
		}
		downloadQueue.add(request);
		processDownloadRequests(callback);
	}

	/**
	 * Retrieves the chain height from the network.
	 * 
	 */
	private void getNetworkChainHeight(Callback<ChainSynchronizationResult> callback) {
		NetworkCallback<GetResponse<Integer>> networkCallback = new NetworkCallback<GetResponse<Integer>>() {
			@Override
			public void onSuccess(GetResponse<Integer> response) {
				List<Integer> retrievedChainHeights = response.getValues();
				Integer maxValue = retrievedChainHeights.get(0);
				for (int i = 1; i < retrievedChainHeights.size(); i++) {
					if (retrievedChainHeights.get(i).intValue() > maxValue.intValue()) {
						maxValue = retrievedChainHeights.get(i);
					}
				}
				synchChainWithNetworkInternal(maxValue, callback);
			}

			@Override
			public void onFailure(GetResponse<Integer> response) {
				log.info("Failed to update chain height from network due to no replies");
				handleFailedHeightRetrieval(ChainSynchronizationResult.NO_REPLIES, callback);
			}
		};
		boolean enoughNodes = networkInterface.getChainHeight(10, 1, networkCallback);
		if (!enoughNodes) {
			needsToUpdateWithEnoughNodes = true;
			log.info("Failed to update chain height from network due to not enough nodes");
			handleFailedHeightRetrieval(ChainSynchronizationResult.NOT_ENOUGH_NODES, callback);
		}
	}

	private void handleFailedHeightRetrieval(ChainSynchronizationResult result, Callback<ChainSynchronizationResult> callback) {
		scheduleChainSynch(CHAIN_SYNCH_DELAY_AFTER_FAILURE);
		callback.onFailure(result);
		this.busy = false;
	}

	private void downloadBlocks(int currentChainHeight, int networkHeight, Callback<ChainSynchronizationResult> callback) {
		for (int i = currentChainHeight + 1; i <= networkHeight; i++) {
			downloadQueue.add(new BlockDownloadRequest(i, null, null));
		}
		processDownloadRequests(callback);
	}

	private void processDownloadRequests(Callback<ChainSynchronizationResult> callback) {
		if (downloadQueue.isEmpty()) {
			// Starts another synch process in case the chain height has changed during the
			// recent one
			synchChainWithNetworkInternal(null, callback);
		} else {
			BlockDownloadRequest request = downloadQueue.poll();
			boolean enoughNodes;
			if (request.blockHash == null) { // Request by the block height
				enoughNodes = networkInterface.getBlocks(request.height.intValue(), 1, 1, new NetworkCallback<GetResponse<List<Block>>>() {
					@Override
					public void onSuccess(GetResponse<List<Block>> response) {
						request.downloadedBlocks = response.getValues().get(0);
						addBlockToChain(request, callback);
					}

					@Override
					public void onFailure(GetResponse<List<Block>> response) {
						onGetBlocksFailure(request, ChainSynchronizationResult.NO_REPLIES, callback);
					}
				});
			} else { // Request by the block hash
				enoughNodes = networkInterface.getBlock(request.blockHash, 1, 1, new NetworkCallback<GetResponse<Block>>() {
					@Override
					public void onSuccess(GetResponse<Block> response) {
						List<Block> blocks = new ArrayList<>(1);
						blocks.add(response.getValues().get(0));
						request.downloadedBlocks = blocks;
						addBlockToChain(request, callback);
					}

					@Override
					public void onFailure(GetResponse<Block> response) {
						onGetBlocksFailure(request, ChainSynchronizationResult.NO_REPLIES, callback);
					}
				});
			}
			if (!enoughNodes) {
				needsToUpdateWithEnoughNodes = true;
				log.info("Failed to retrieve block(s) from network due to not enough nodes");
				onGetBlocksFailure(request, ChainSynchronizationResult.NOT_ENOUGH_NODES, callback);
			}
		}
	}

	private void onGetBlocksFailure(BlockDownloadRequest request, ChainSynchronizationResult result, Callback<ChainSynchronizationResult> callback) {
		request.numberOfFailures++;
		if (request.numberOfFailures == MAX_ALLOWED_NUMBER_OF_FAILURES || result == ChainSynchronizationResult.NOT_ENOUGH_NODES) {
			callback.onFailure(result);
			this.busy = false;
		} else {
			downloadQueue.add(request); // Re-adds the request to the queue to be processed again
			processDownloadRequests(callback);
		}
	}

	private void addBlockToChain(BlockDownloadRequest request, Callback<ChainSynchronizationResult> callback) {
		for (Block block : request.downloadedBlocks) {
			Block previousBlock = block.getPreviousBlock();
			if (chain.isBlockExists(previousBlock.getHash())) {
				chain.addBlock(block);
				if (request.nextBlockDownloadRequest != null) {
					addBlockToChain(request.nextBlockDownloadRequest, callback);
				}
			} else {
				// The purpose of making another BlockDownloadRequest object is for it to have
				// no more than one block. If there is more than one, the method that adds to
				// the chain will not know which one to add
				BlockDownloadRequest modifiedRequest = new BlockDownloadRequest(request.height, request.blockHash, request.nextBlockDownloadRequest, block);
				downloadQueue.add(new BlockDownloadRequest(request.height - 1, previousBlock.getHash(), modifiedRequest));
			}
		}

		processDownloadRequests(callback);
	}

	private void scheduleChainSynch(long delay) {
		if (alwaysRunning) {
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					if (isBusy()) {
						scheduleChainSynch(CHAIN_SYNCH_DELAY_IF_BUSY);
					} else {
						synchChainWithNetwork();
					}
				}
			}, delay);
		}
	}

	@Override
	public void onAvailability() {
		if (needsToUpdateWithEnoughNodes) {
			needsToUpdateWithEnoughNodes = false;
			if (isBusy()) {
				scheduleChainSynch(CHAIN_SYNCH_DELAY_IF_BUSY);
			} else {
				synchChainWithNetwork();
			}
		}
	}

	private static class BlockDownloadRequest implements Comparable<BlockDownloadRequest> {

		public Integer height;
		public byte[] blockHash;
		public BlockDownloadRequest nextBlockDownloadRequest;
		public List<Block> downloadedBlocks;
		public int numberOfFailures;

		public BlockDownloadRequest(int height, byte[] blockHash, BlockDownloadRequest nextBlockDownloadRequest) {
			this.height = new Integer(height);
			this.blockHash = blockHash;
			this.nextBlockDownloadRequest = nextBlockDownloadRequest;
		}

		public BlockDownloadRequest(int height, byte[] blockHash, BlockDownloadRequest nextBlockDownloadRequest, Block downloadedBlock) {
			this.height = new Integer(height);
			this.blockHash = blockHash;
			this.nextBlockDownloadRequest = nextBlockDownloadRequest;
			if (downloadedBlock != null) {
				downloadedBlocks = new ArrayList<>();
				downloadedBlocks.add(downloadedBlock);
			}
		}

		@Override
		public int compareTo(BlockDownloadRequest o) {
			return height.compareTo(o.height);
		}
	}
}
