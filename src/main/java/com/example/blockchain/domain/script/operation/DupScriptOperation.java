package com.example.blockchain.domain.script.operation;

import java.util.Stack;

import com.example.blockchain.domain.transaction.Transaction;
import com.example.blockchain.util.ByteUtil;

public class DupScriptOperation implements ScriptOperation {

	@Override
	public boolean run(Stack<byte[]> stack, Transaction tx, int inputIndex) {
		byte[] topValue = stack.peek();
		stack.push(ByteUtil.cloneByteArray(topValue));
		return true;
	}
}
