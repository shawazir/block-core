package com.example.blockchain.domain.script.operation;

import java.util.Stack;

import com.example.blockchain.domain.transaction.Transaction;

public interface ScriptOperation {

	boolean run(Stack<byte[]> stack, Transaction tx, int inputIndex);
}
