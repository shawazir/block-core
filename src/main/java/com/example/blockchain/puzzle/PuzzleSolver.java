package com.example.blockchain.puzzle;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import com.example.blockchain.difficulty.TargetCalculator;
import com.example.blockchain.domain.block.Bits;
import com.example.blockchain.domain.block.Block;

public class PuzzleSolver {

	private boolean isRunning = false;
	private Set<PuzzleSolvedSubscriber> subscribers = new HashSet<>();
	private TargetCalculator targetCalculator;

	public PuzzleSolver(TargetCalculator targetCalculator) {
		this.targetCalculator = targetCalculator;
	}

	/**
	 * Subscribes to the event of solving a block.
	 * 
	 */
	public void subscribe(PuzzleSolvedSubscriber subscriber) {
		subscribers.add(subscriber);
	}

	/**
	 * Returns whether the solver is running or not.
	 * 
	 */
	public boolean isRunning() {
		return isRunning;
	}

	/**
	 * Starts solving the nonce of the given block.
	 * 
	 */
	public void start(Block candidateBlock) {
		if (isRunning) {
			throw new IllegalStateException("Puzzle solver already started");
		}

		Thread thread = new Thread() {
			public void run() {
				solvePuzzle(candidateBlock);
			}
		};
		thread.start();
	}

	/**
	 * Solves the puzzle.
	 * 
	 */
	boolean solvePuzzle(Block candidateBlock) {
		// TODO Is it possible that the nonce exceeds Long.MAX_VALUE ?
		isRunning = true;
		BigInteger target = targetCalculator.calculateTarget().getValue();
		candidateBlock.setBits(new Bits(target));
		long counter = 0;
		do {
			if (!isRunning) {
				return false;
			}
			candidateBlock.setNonce(counter);
			candidateBlock.setHash(true);
			counter++;
		} while (new BigInteger(1, candidateBlock.getHash()).compareTo(target) == 1);
		isRunning = false;
		notifySubscribers(candidateBlock);
		return true;
	}

	/**
	 * Notifies the subscribers of solving the block.
	 * 
	 */
	private void notifySubscribers(Block block) {
		for (PuzzleSolvedSubscriber subscriber : subscribers) {
			subscriber.puzzleSolved(block);
		}
	}

	/**
	 * Stops solving the nonce.
	 * 
	 */
	public void stop() {
		this.isRunning = false;
	}
}
