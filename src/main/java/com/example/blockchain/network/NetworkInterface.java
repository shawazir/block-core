package com.example.blockchain.network;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.Range;

import com.example.blockchain.domain.block.Block;
import com.example.blockchain.domain.transaction.Transaction;
import com.example.blockchain.network.handler.GetResponse;

public interface NetworkInterface {

	void subscribeNodeAvailabilitySubscriber(NodeAvailabilitySubscriber subscriber, int targetAvailability);

	void unsubscribeNodeAvailabilitySubscriber(NodeAvailabilitySubscriber subscriber, int targetAvailability);

	void relayBlock(Block block);

	boolean getNetworkTime(int preferredNumberOfNodes, int minNumberOfNodes, NetworkCallback<GetResponse<Date>> networkCallback);

	boolean getPendingTransactions(int preferredNumberOfNodes, int minNumberOfNodes, NetworkCallback<GetResponse<List<Transaction>>> networkCallback);

	boolean getChainHeight(int preferredNumberOfNodes, int minNumberOfNodes, NetworkCallback<GetResponse<Integer>> networkCallback);

	boolean getBlock(byte[] blockHash, int preferredNumberOfNodes, int minNumberOfNodes, NetworkCallback<GetResponse<Block>> networkCallback);

	boolean getBlocks(int height, int preferredNumberOfNodes, int minNumberOfNodes, NetworkCallback<GetResponse<List<Block>>> networkCallback);

	boolean getBlocks(Range<Integer> heightRange, int preferredNumberOfNodes, int minNumberOfNodes, NetworkCallback<GetResponse<List<Block>>> networkCallback);
}
