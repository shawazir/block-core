package com.example.blockchain.domain.block;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.example.blockchain.dao.BlockDao;
import com.example.blockchain.domain.block.builder.BlockBuilder;
import com.example.blockchain.domain.block.builder.BlockBuilderV1;
import com.example.blockchain.domain.block.builder.BuildBlockForm;
import com.example.blockchain.domain.transaction.Transaction;
import com.example.blockchain.util.ConvertUtil;
import com.example.blockchain.util.HexStringUtil;
import com.example.blockchain.util.Sha256Util;

public class Block {

	public static final int DEFAULT_MAX_SIZE = 1048576; // One megabytes

	private byte[] hash;
	private short version;
	private Block previousBlock;
	private byte[] merkleRoot;
	private Date time;
	private Bits bits;
	private long nonce;
	private Transaction[] transactions;

	private byte[] cachedBlockHeader;
	private boolean headerLoaded = false;
	private boolean transactionsLoaded = false;

	public static BlockDao blockDao;

	private static BlockBuilder blockBuilder = new BlockBuilderV1();

	public Block(byte[] hash) {
		this.hash = hash;
		this.headerLoaded = false;
		this.transactionsLoaded = false;
	}

	public Block(short version, Block previousBlock, byte[] merkleRoot, Date time, Bits bits, long nonce) {
		this.version = version;
		this.previousBlock = previousBlock;
		this.merkleRoot = merkleRoot;
		this.time = time;
		this.bits = bits;
		this.nonce = nonce;
		this.headerLoaded = true;
		this.transactionsLoaded = false;
		this.cachedBlockHeader = blockBuilder.serializeBlockHeader(this);
		setHash();
	}

	public Block(short version, Block previousBlock, byte[] merkleRoot, Date time, Bits bits, long nonce, Transaction[] transactions) {
		this.version = version;
		this.previousBlock = previousBlock;
		this.transactions = transactions;
		this.merkleRoot = merkleRoot;
		this.time = time;
		this.bits = bits;
		this.nonce = nonce;
		this.headerLoaded = true;
		this.transactionsLoaded = true;
		this.cachedBlockHeader = blockBuilder.serializeBlockHeader(this);
		setHash();
	}

	public Block(byte[] hash, short version, Block previousBlock, byte[] merkleRoot, Date time, Bits bits, long nonce) {
		this.hash = hash;
		this.version = version;
		this.previousBlock = previousBlock;
		this.merkleRoot = merkleRoot;
		this.time = time;
		this.bits = bits;
		this.nonce = nonce;
		this.headerLoaded = true;
		this.transactionsLoaded = false;
		this.cachedBlockHeader = blockBuilder.serializeBlockHeader(this);
	}

	public Block(byte[] hash, short version, Block previousBlock, byte[] merkleRoot, Date time, Bits bits, long nonce, Transaction[] transactions) {
		this.hash = hash;
		this.version = version;
		this.previousBlock = previousBlock;
		this.transactions = transactions;
		this.merkleRoot = merkleRoot;
		this.time = time;
		this.bits = bits;
		this.nonce = nonce;
		this.headerLoaded = true;
		this.transactionsLoaded = true;
		this.cachedBlockHeader = blockBuilder.serializeBlockHeader(this);
	}

	/**
	 * Sets the BlockDao.
	 *
	 */
	public static void setBlockDao(BlockDao blockDao) {
		Block.blockDao = blockDao;
	}

	/*
	 * Creates a new block.
	 * 
	 */
	public static Block createCandidateBlock(BuildBlockForm buildBlockForm) {
		return blockBuilder.buildBlock(buildBlockForm);
	}

	/**
	 * Builds a block from the given data. If transactions are included, then they
	 * are included empty with nothing but the IDs.
	 * 
	 */
	public static Block buildBlock(byte[] blockData, boolean deserializeTransactionIds) {
		// TODO Handle invalid blockData
		if (!deserializeTransactionIds) {
			return blockBuilder.deserializeBlockHeader(blockData);
		} else {
			return blockBuilder.deserializeBlock(blockData);
		}
	}

	/**
	 * Builds the block hash for the data in the given byte array.
	 * 
	 */
	public static byte[] buildBlockHash(byte[] blockHeaderData) {
		byte[] blockHash = Sha256Util.doubleHash(blockHeaderData);
		return blockHash;
	}

	/**
	 * Builds the block hash for the data in the given string.
	 * 
	 */
	public static String buildBlockHash(String blockHeaderData) {
		byte[] blockHeaderDataByteArray = ConvertUtil.hexStringToByteArray(blockHeaderData);
		byte[] blockHashByteArray = buildBlockHash(blockHeaderDataByteArray);
		return ConvertUtil.byteArrayToHexString(blockHashByteArray);
	}

	/**
	 * Serializes the given list of blocks.
	 * 
	 */
	public static byte[] serializeMultipleBlocks(List<Block> blocks, boolean includeTransactions) {
		return blockBuilder.serializeMultipleBlocks(blocks, includeTransactions);
	}

	/**
	 * Deserializes the given blocks data.
	 * 
	 */
	public static List<Block> deserializeMultipleBlocks(byte[] blockData) {
		return blockBuilder.deserializeMultipleBlocks(blockData);
	}

	/**
	 * Returns whether the block is the genesis block or not.
	 * 
	 */
	public boolean isGenesis() {
		return false;
	}

	/**
	 * Sets the hash of the block.
	 * 
	 */
	public void setHash() {
		setHash(false);
	}

	/**
	 * Sets the hash of the block.
	 * 
	 */
	public void setHash(boolean enforce) {
		if (this.hash == null || enforce == true) {
			byte[] blockHeaderData = blockBuilder.serializeBlockHeader(this);
			this.cachedBlockHeader = blockHeaderData;
			this.hash = buildBlockHash(blockHeaderData);
		}
	}

	/**
	 * Serializes the block.
	 * 
	 */
	public byte[] serialize(boolean includeTransactions) {
		if (includeTransactions) {
			return blockBuilder.serializeBlock(this).getData();
		} else {
			return blockBuilder.serializeBlockHeader(this);
		}
	}

	/**
	 * Validates the block.
	 * 
	 */
	public boolean validate() {
		// TODO Implement this
		return false;
	}

	/**
	 * Loads the data of the block. If the data is already loaded, nothing is done.
	 * 
	 */
	public void load(boolean includeTransactions) {
		if (includeTransactions) {
			if (!transactionsLoaded) {
				loadData(true);
			}
		} else {
			if (!headerLoaded) {
				loadData(false);
			}
		}
	}

	/**
	 * Loads the data of the block. If the data is already loaded, nothing is done.
	 * 
	 */
	private void loadData(boolean includeTransactions) {
		// TODO Handle multiple concurrent requests to load the data
		if (!headerLoaded) {
			Block block = blockDao.findBlock(this.hash, includeTransactions);
			this.version = block.getVersion();
			this.previousBlock = block.getPreviousBlock();
			this.transactions = block.getTransactions();
			transactionsLoaded = true;
			this.merkleRoot = block.getMerkleRoot();
			this.time = block.getTime();
			this.bits = block.getBits();
			this.nonce = block.getNonce();
			this.headerLoaded = true;
			if (includeTransactions) {
				this.transactions = blockDao.findBlockTransactions(this);
			}
		} else if (headerLoaded && !transactionsLoaded && includeTransactions) {
			this.transactions = blockDao.findBlockTransactions(this);
			transactionsLoaded = true;
		}
	}

	/**
	 * Gets the size of the block in bytes.
	 * 
	 */
	public int getSize() {
		int size = 0;
		int headerSize = cachedBlockHeader.length;
		size += headerSize;
		int varIntSize = HexStringUtil.buildVarInt(transactions.length).length;
		size += varIntSize;
		for (int i = 0; i < transactions.length; i++) {
			size += transactions[i].getSize();
		}
		return size;
	}

	/**
	 * Nullifies the transactions array.
	 * 
	 */
	public void nullifyTransactions() {
		this.transactions = null;
	}

	/**
	 * Returns whether transactions is null or not.
	 * 
	 */
	public boolean isTransactionsNull() {
		return this.transactions == null;
	}

	// Object METHODS // --------------------------------------------

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(hash);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Block other = (Block) obj;
		if (!Arrays.equals(hash, other.hash))
			return false;
		return true;
	}

	// GETTERS & SETTERS // -----------------------------------------

	public byte[] getHash() {
		return hash;
	}

	public short getVersion() {
		if (!headerLoaded) {
			load(false);
		}
		return version;
	}

	public Block getPreviousBlock() {
		if (!headerLoaded) {
			load(false);
		}
		return previousBlock;
	}

	public byte[] getMerkleRoot() {
		if (!headerLoaded) {
			load(false);
		}
		return merkleRoot;
	}

	public Date getTime() {
		if (!headerLoaded) {
			load(false);
		}
		return time;
	}

	public Bits getBits() {
		if (!headerLoaded) {
			load(false);
		}
		return bits;
	}

	public void setBits(Bits bits) {
		this.bits = bits;
	}

	public long getNonce() {
		if (!headerLoaded) {
			load(false);
		}
		return nonce;
	}

	public void setNonce(long nonce) {
		this.nonce = nonce;
	}

	public Transaction[] getTransactions() {
		if (!transactionsLoaded) {
			load(true);
		}
		return transactions;
	}
}
