package com.example.blockchain.domain.script.operation;

import java.util.Stack;

import org.bouncycastle.util.Arrays;

import com.example.blockchain.domain.transaction.Transaction;

public class EqualVerifyScriptOperation implements ScriptOperation {

	@Override
	public boolean run(Stack<byte[]> stack, Transaction tx, int inputIndex) {
		byte[] value1 = stack.pop();
		byte[] value2 = stack.pop();
		if (Arrays.areEqual(value1, value2)) {
			return true;
		} else {
			return false;
		}
	}
}
