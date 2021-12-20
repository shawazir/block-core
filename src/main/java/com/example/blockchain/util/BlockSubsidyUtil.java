package com.example.blockchain.util;

import java.math.BigDecimal;
import java.math.BigInteger;

public class BlockSubsidyUtil {

	private static final int INITIAL_SUBSIDY = 50;
	private static final int BLOCKS_INTERVAL = 210000;

	/**
	 * Calculates the subsidy in satoshis.
	 * 
	 */
	public static BigInteger calculateSubsidyInSatoshis(int blockHeight) {
		double subsidy = calculateSubsidy(blockHeight);
		// FIXME Mind the loss in accuracy when converting from BigDecimal to BigInteger
		BigInteger subsidyInSatoshis = BigDecimal.valueOf(subsidy).multiply(BigDecimal.valueOf(100000000)).toBigInteger();
		return subsidyInSatoshis;
	}

	/**
	 * Calculates the subsidy.
	 * 
	 */
	public static double calculateSubsidy(int blockHeight) {
		// FIXME Fix how the reward approaches zero
		int numberOfHalvings = calculateNumberOfHalvings(blockHeight);
		double subsidy = INITIAL_SUBSIDY / Math.pow(2, numberOfHalvings);
		return subsidy;
	}

	private static int calculateNumberOfHalvings(int blockHeight) {
		int numberOfHalvings = 0;
		while (blockHeight > BLOCKS_INTERVAL * (numberOfHalvings + 1)) {
			numberOfHalvings++;
		}
		return numberOfHalvings;
	}
}
