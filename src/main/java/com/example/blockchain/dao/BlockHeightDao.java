package com.example.blockchain.dao;

import java.util.List;

import com.example.blockchain.domain.block.Block;

public interface BlockHeightDao {

	List<byte[]> findBlockHashesAtHeight(int height);

	void saveBlockHeight(Block block, int height);

	void saveBlockHeight(byte[] blockHash, int height);
}
