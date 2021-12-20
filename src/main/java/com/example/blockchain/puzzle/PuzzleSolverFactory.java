package com.example.blockchain.puzzle;

import com.example.blockchain.difficulty.TargetCalculator;

public class PuzzleSolverFactory {

	// Non usable constructor
	private PuzzleSolverFactory() {

	}

	public static PuzzleSolver createPuzzleSolver(TargetCalculator targetCalculator) {
		return new PuzzleSolver(targetCalculator);
	}
}
