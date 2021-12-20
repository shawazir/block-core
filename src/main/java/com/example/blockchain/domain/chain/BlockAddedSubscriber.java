package com.example.blockchain.domain.chain;

import com.example.blockchain.domain.block.Block;

public interface BlockAddedSubscriber {

	void blockAdded(Block block, int height, double chainWork, boolean isActiveTip, boolean minedLocally);
}
