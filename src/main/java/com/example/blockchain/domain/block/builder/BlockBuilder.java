package com.example.blockchain.domain.block.builder;

import java.util.List;

import com.example.blockchain.domain.block.Block;

public interface BlockBuilder {

	Block buildBlock(BuildBlockForm buildBlockForm);

	boolean validateBlock(Block block);

	int calculateBlockHeaderSize(Block block);

	int calculateBlockSize(Block block);

	byte[] serializeBlockHeader(Block block);

	BlockSerializeResult serializeBlock(Block block);

	byte[] serializeMultipleBlocks(List<Block> blocks, boolean includeTransactions);

	Block deserializeBlockHeader(byte[] data);

	BlockBuildResult deserializeBlockHeader(byte[] data, int dataStartIndex);

	Block deserializeBlock(byte[] data);

	BlockBuildResult deserializeBlock(byte[] data, int dataStartIndex);

	List<Block> deserializeMultipleBlocks(byte[] data);
}
