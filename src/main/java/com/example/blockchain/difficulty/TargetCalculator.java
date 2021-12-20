package com.example.blockchain.difficulty;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.example.blockchain.domain.block.Block;
import com.example.blockchain.domain.chain.Chain;

public class TargetCalculator {

	private static final int TARGET_ADJUST_COUNT = 2016;
	private static final long IDEAL_TIME_INTERVAL = Chain.DURATION_BETWEEN_BLOCKS * TARGET_ADJUST_COUNT; // Two weeks in milliseconds

	private Chain chain;

	public TargetCalculator(Chain chain) {
		this.chain = chain;
	}

	public Target getTarget() {
		return calculateTarget();
	}

	/**
	 * Calculates the target.
	 * 
	 */
	public Target calculateTarget() {
		// Returns the maximum target if there are too few blocks
		int currentChainHeight = chain.getHeight();
		boolean keepInitialTaregt = currentChainHeight < TARGET_ADJUST_COUNT;
		if (keepInitialTaregt) {
			return new Target(Target.MAX_TARGET_VALUE);
		}

		// Finds the difference in time and calculates the change ratio
		int heightOfLastTargetAdjustBlock = (int) Math.floor(currentChainHeight / TARGET_ADJUST_COUNT) * TARGET_ADJUST_COUNT;
		int heightOfFirstTargetAdjustBlock = heightOfLastTargetAdjustBlock - TARGET_ADJUST_COUNT;
		Block firstTargetAdjustBlock = chain.getActiveBlock(heightOfFirstTargetAdjustBlock);
		Block lastTargetAdjustBlock = chain.getActiveBlock(heightOfLastTargetAdjustBlock);
		long timestampOfFirstTargetAdjustBlock = firstTargetAdjustBlock.getTime().getTime();
		long timestampOfLastTargetAdjustBlock = lastTargetAdjustBlock.getTime().getTime();
		long differenceInTime = timestampOfLastTargetAdjustBlock - timestampOfFirstTargetAdjustBlock;
		double changeRatio = (double) differenceInTime / (double) IDEAL_TIME_INTERVAL;

		// Limits the change ratio if it's too large
		if (changeRatio < 0.25) {
			changeRatio = 0.25;
		}
		if (changeRatio > 4) {
			changeRatio = 4;
		}

		// Multiplies the current target by the change ratio to get the new target
		BigInteger currentTargetValue = lastTargetAdjustBlock.getBits().toTargetValue();
		currentTargetValue = new BigDecimal(currentTargetValue).multiply(BigDecimal.valueOf(changeRatio)).toBigInteger();

		// Makes sure the new target does not go above the maximum target
		if (currentTargetValue.compareTo(Target.MAX_TARGET_VALUE) > 0) {
			return new Target(Target.MAX_TARGET_VALUE);
		} else {
			return new Target(truncateTarget(currentTargetValue));
		}
	}

	/**
	 * Truncates the given target.
	 * 
	 */
	private BigInteger truncateTarget(BigInteger target) {
		int length = target.toString(16).length();
		if (length % 2 != 0) {
			length++;
		}
		int shiftAmount = (length - 6) / 2 * 8;
		BigInteger truncatedTarget = target.shiftRight(shiftAmount).shiftLeft(shiftAmount);
		return truncatedTarget;
	}
}
