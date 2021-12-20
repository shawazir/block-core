package com.example.blockchain.domain.block;

import java.util.Date;

import com.example.blockchain.difficulty.Target;
import com.example.blockchain.domain.transaction.Transaction;

public class GenesisBlock extends Block {

	private static final Block instance = new GenesisBlock();

	private GenesisBlock() {
		super((short) 1, new Block(new byte[32]), new byte[32], new Date(1617033110386L), new Bits(Target.MAX_TARGET_VALUE), 269029189, new Transaction[0]);
	}

	/**
	 * Returns the only genesis block instance.
	 * 
	 */
	public static Block getInstance() {
		return instance;
	}

	/**
	 * Returns whether the block is the genesis block or not.
	 * 
	 */
	public boolean isGenesis() {
		return true;
	}
}
