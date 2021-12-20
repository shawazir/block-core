package com.example.blockchain.puzzle;

import com.example.blockchain.domain.block.Block;

public interface PuzzleSolvedSubscriber {

	void puzzleSolved(Block block);
}
