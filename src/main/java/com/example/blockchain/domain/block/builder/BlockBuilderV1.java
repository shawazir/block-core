package com.example.blockchain.domain.block.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.example.blockchain.domain.block.Bits;
import com.example.blockchain.domain.block.Block;
import com.example.blockchain.domain.block.GenesisBlock;
import com.example.blockchain.domain.block.builder.validator.BlockBuilderV1Validator;
import com.example.blockchain.domain.block.builder.validator.BuildBlockFormValidationResult;
import com.example.blockchain.domain.block.builder.validator.InvalidBuildBlockFormException;
import com.example.blockchain.domain.transaction.Transaction;
import com.example.blockchain.domain.transaction.builder.TransactionBuildResult;
import com.example.blockchain.util.ByteUtil;
import com.example.blockchain.util.HexStringUtil;
import com.example.blockchain.util.Sha256Util;

public class BlockBuilderV1 implements BlockBuilder {

	static final short VERSION = 1;

	private static final int VERSION_SIZE = 1; // In bytes
	private static final int PREVIOUS_BLOCK_SIZE = 32; // In bytes
	private static final int MERKLE_ROOT_SIZE = 32; // In bytes
	private static final int TIME_SIZE = 4; // In bytes
	public static final int BITS_SIZE = 4; // In bytes
	private static final int NONCE_SIZE = 4; // In bytes
	public static final int BLOCK_HEADER_SIZE = VERSION_SIZE + PREVIOUS_BLOCK_SIZE + MERKLE_ROOT_SIZE + TIME_SIZE + BITS_SIZE + NONCE_SIZE; // In bytes

	private BlockBuilderV1Validator blockBuilderV1Validator = new BlockBuilderV1Validator();

	@Override
	public Block buildBlock(BuildBlockForm form) {
		// Validates the form
		BuildBlockFormValidationResult validationResult = blockBuilderV1Validator.validateBuildBlockForm(form);
		if (validationResult != BuildBlockFormValidationResult.SUCCESS) {
			throw new InvalidBuildBlockFormException(validationResult);
		}

		// Builds the block
		Block previousBlock = form.getPreviousBlock();
		Transaction[] transactions = form.getTransactions();
		byte[] merkleRoot = constructMerkleRoot(transactions);
		Date time = form.getTime();
		Bits bits = new Bits(form.getTarget());
		long nonce = 0;
		Block block = new Block(VERSION, previousBlock, merkleRoot, time, bits, nonce, transactions);
		return block;
	}

	private byte[] constructMerkleRoot(Transaction[] transactions) {
		byte[][] txIdsArray = new byte[transactions.length][];
		for (int i = 0; i < transactions.length; i++) {
			txIdsArray[i] = transactions[i].getId();
		}
		byte[] merkleRoot = mergeAndHashTrabsactionIds(txIdsArray);
		return merkleRoot;
	}

	private byte[] mergeAndHashTrabsactionIds(byte[][] transactionIds) {
		if (transactionIds.length == 1) {
			return transactionIds[0];
		} else {
			int numOfIdHashes;
			if (transactionIds.length % 2 == 0) {
				numOfIdHashes = transactionIds.length / 2;
			} else {
				numOfIdHashes = transactionIds.length / 2 + 1;
			}
			byte[][] outputTxIds = new byte[numOfIdHashes][];
			for (int i = 0; i < numOfIdHashes * 2; i += 2) {
				if (transactionIds.length == (i + 1)) {
					outputTxIds[i / 2] = transactionIds[i];
				} else {
					outputTxIds[i / 2] = mergeAndHashTrabsactionIds(transactionIds[i], transactionIds[i + 1]);
				}
			}
			return mergeAndHashTrabsactionIds(outputTxIds);
		}
	}

	private byte[] mergeAndHashTrabsactionIds(byte[] txId1, byte[] txId2) {
		byte[] mergedIds = new byte[txId1.length + txId2.length];
		for (int i = 0; i < txId1.length; i++) {
			mergedIds[i] = txId1[i];
		}
		for (int i = 0; i < txId2.length; i++) {
			mergedIds[txId1.length + i] = txId2[i];
		}
		return Sha256Util.doubleHash(mergedIds);
	}

	@Override
	public boolean validateBlock(Block block) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int calculateBlockHeaderSize(Block block) {
		int size = 0;
		size += VERSION_SIZE;
		size += block.getPreviousBlock().getHash().length;
		size += block.getMerkleRoot().length;
		size += TIME_SIZE;
		size += BITS_SIZE;
		size += NONCE_SIZE;
		return size;
	}

	@Override
	public int calculateBlockSize(Block block) {
		int size = BLOCK_HEADER_SIZE;
		int transactionsCount = block.getTransactions().length;
		byte[] transactionsCountByteArray = HexStringUtil.buildVarInt(transactionsCount);
		size += transactionsCountByteArray.length;
		for (Transaction transaction : block.getTransactions()) {
			size += transaction.getSize();
		}
		return size;
	}

	/**
	 * Serializes the header of the given block.
	 * 
	 */
	@Override
	public byte[] serializeBlockHeader(Block block) {
		byte[] data = new byte[BLOCK_HEADER_SIZE];
		int nextIndex = 0;

		// Version
		byte[] bytes = ByteUtil.convertToByteArray(block.getVersion(), VERSION_SIZE);
		System.arraycopy(bytes, 0, data, nextIndex, VERSION_SIZE);
		nextIndex += VERSION_SIZE;

		// Previous Block Hash
		System.arraycopy(block.getPreviousBlock().getHash(), 0, data, nextIndex, block.getPreviousBlock().getHash().length);
		nextIndex += block.getPreviousBlock().getHash().length;

		// Merkle Root
		System.arraycopy(block.getMerkleRoot(), 0, data, nextIndex, block.getMerkleRoot().length);
		nextIndex += block.getMerkleRoot().length;

		// Time
		bytes = ByteUtil.convertToByteArray(block.getTime().getTime() / 1000, TIME_SIZE);
		System.arraycopy(bytes, 0, data, nextIndex, TIME_SIZE);
		nextIndex += TIME_SIZE;

		// Bits
		System.arraycopy(block.getBits().getValue(), 0, data, nextIndex, BITS_SIZE);
		nextIndex += BITS_SIZE;

		// Nonce
		bytes = ByteUtil.convertToByteArray(block.getNonce(), NONCE_SIZE);
		System.arraycopy(bytes, 0, data, nextIndex, NONCE_SIZE);
		nextIndex += NONCE_SIZE;

		return data;
	}

	/**
	 * Serializes the given block.
	 * 
	 */
	@Override
	public BlockSerializeResult serializeBlock(Block block) {
		int blockSize = calculateBlockSize(block);
		byte[] data = new byte[blockSize];
		int nextIndex = 0;

		// Appends the Block Header
		byte[] bytes = serializeBlockHeader(block);
		System.arraycopy(bytes, 0, data, nextIndex, BLOCK_HEADER_SIZE);
		nextIndex += BLOCK_HEADER_SIZE;

		// Appends the Transactions Count VarInt
		byte[] transactionsCountVarInt = HexStringUtil.buildVarInt(block.getTransactions().length);
		System.arraycopy(transactionsCountVarInt, 0, data, nextIndex, transactionsCountVarInt.length);
		nextIndex += transactionsCountVarInt.length;

		// Appends the Transactions
		for (Transaction tx : block.getTransactions()) {
			System.arraycopy(tx.serialize(), 0, data, nextIndex, tx.getSize());
			nextIndex += tx.getSize();
		}

		return new BlockSerializeResult(data, nextIndex);
	}

	/**
	 * Serializes the given list of blocks.
	 * 
	 */
	@Override
	public byte[] serializeMultipleBlocks(List<Block> blocks, boolean includeTransactions) {
		// Calculates the count VarInt
		byte[] blocksCountVarInt = HexStringUtil.buildVarInt(blocks.size());

		// Calculates the total data size
		int dataSize = 0;
		if (includeTransactions) {
			int blocksSize = calculateBlocksSize(blocks);
			dataSize = blocksCountVarInt.length + blocksSize;
		} else {
			dataSize = BLOCK_HEADER_SIZE * blocks.size();
		}

		// Instantiates the data byte array.
		byte[] data = new byte[dataSize];

		// Appends the blocks count VarInt
		int nextIndex = 0;
		System.arraycopy(blocksCountVarInt, 0, data, nextIndex, blocksCountVarInt.length);
		nextIndex += blocksCountVarInt.length;

		// Appends the block data
		for (Block block : blocks) {
			byte[] blockData = block.serialize(includeTransactions);
			System.arraycopy(blockData, 0, data, nextIndex, blockData.length);
			nextIndex += blockData.length;
		}

		// Returns
		return data;
	}

	private int calculateBlocksSize(List<Block> blocks) {
		int size = 0;
		for (Block block : blocks) {
			size += calculateBlockSize(block);
		}
		return size;
	}

	/**
	 * Converts the given block header data to its object form.
	 * 
	 */
	@Override
	public Block deserializeBlockHeader(byte[] data) {
		return deserializeBlockHeader(data, 0).getBlock();
	}

	/**
	 * Converts the given block header data to its object form starting with the
	 * given index.
	 * 
	 */
	@Override
	public BlockBuildResult deserializeBlockHeader(byte[] data, int dataStartIndex) {
		// Version
		int nextIndex = dataStartIndex;
		short version = ByteUtil.getShort(data, nextIndex, VERSION_SIZE);
		nextIndex += VERSION_SIZE;

		// Previous Block
		byte[] previousBlockHash = ByteUtil.getBytes(data, nextIndex, PREVIOUS_BLOCK_SIZE);
		Block previousBlock;
		if (Arrays.equals(previousBlockHash, GenesisBlock.getInstance().getHash())) {
			previousBlock = GenesisBlock.getInstance();
		} else {
			previousBlock = new Block(previousBlockHash);
		}
		nextIndex += PREVIOUS_BLOCK_SIZE;

		// Merkle Root
		byte[] merkleRoot = ByteUtil.getBytes(data, nextIndex, MERKLE_ROOT_SIZE);
		nextIndex += MERKLE_ROOT_SIZE;

		// Time
		long timestampInSeconds = ByteUtil.getLong(data, nextIndex, TIME_SIZE);
		nextIndex += TIME_SIZE;
		Date time = new Date(timestampInSeconds * 1000);

		// Bits
		Bits bits = new Bits(ByteUtil.getBytes(data, nextIndex, BITS_SIZE));
		nextIndex += BITS_SIZE;

		// Nonce
		long nonce = ByteUtil.getLong(data, nextIndex, NONCE_SIZE);
		nextIndex += NONCE_SIZE;

		Block block = new Block(version, previousBlock, merkleRoot, time, bits, nonce);
		return new BlockBuildResult(block, nextIndex);
	}

	/**
	 * Converts the given block data to its object form.
	 * 
	 */
	@Override
	public Block deserializeBlock(byte[] data) {
		return deserializeBlock(data, 0).getBlock();
	}

	/**
	 * Converts the given block data to its object form starting with the given
	 * index.
	 * 
	 */
	@Override
	public BlockBuildResult deserializeBlock(byte[] data, int dataStartIndex) {
		BlockBuildResult blockBuildResult = deserializeBlockHeader(data, dataStartIndex);
		int nextIndex = blockBuildResult.getNextIndex();
		Block blockWithHeaderOnly = blockBuildResult.getBlock();

		int transactionsCountSize = HexStringUtil.getLengthOfVarInt(data, nextIndex);
		int transactionsCount = HexStringUtil.parseVarInt(ByteUtil.getBytes(data, nextIndex, transactionsCountSize)).intValue();
		nextIndex += transactionsCountSize;
		Transaction[] transactions = new Transaction[transactionsCount];
		for (int i = 0; i < transactionsCount; i++) {
			TransactionBuildResult result = Transaction.buildTransaction(data, nextIndex);
			transactions[i] = result.getTransaction();
			nextIndex = result.getNextIndex();
		}
		Block block = new Block(blockWithHeaderOnly.getVersion(), blockWithHeaderOnly.getPreviousBlock(), blockWithHeaderOnly.getMerkleRoot(),
				blockWithHeaderOnly.getTime(), blockWithHeaderOnly.getBits(), blockWithHeaderOnly.getNonce(), transactions);

		return new BlockBuildResult(block, nextIndex);
	}

	/**
	 * Converts the given blocks data to its object form in a List.
	 * 
	 */
	@Override
	public List<Block> deserializeMultipleBlocks(byte[] data) {
		int nextIndex = 0;
		int blocksCountSize = HexStringUtil.getLengthOfVarInt(data);
		int blocksCount = HexStringUtil.parseVarInt(ByteUtil.getBytes(data, 0, blocksCountSize)).intValue();
		nextIndex += blocksCountSize;
		List<Block> blocks = new ArrayList<Block>(blocksCount);
		for (int i = 0; i < blocksCount; i++) {
			BlockBuildResult result = deserializeBlock(data, nextIndex);
			blocks.add(result.getBlock());
			nextIndex = result.getNextIndex();
		}
		return blocks;
	}
}
