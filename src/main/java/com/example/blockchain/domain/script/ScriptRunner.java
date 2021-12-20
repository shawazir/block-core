package com.example.blockchain.domain.script;

import java.util.Arrays;
import java.util.Stack;

import com.example.blockchain.domain.script.operation.ScriptOperation;
import com.example.blockchain.domain.script.operation.ScriptOperationFactory;
import com.example.blockchain.domain.transaction.Transaction;
import com.example.blockchain.util.ByteUtil;

public class ScriptRunner {

	private static final byte[] successfulFinalResult = new byte[] { 1 };

	private ScriptOperationFactory scriptOperationFactory;

	public ScriptRunner(ScriptOperationFactory scriptOperationFactory) {
		this.scriptOperationFactory = scriptOperationFactory;
	}

	public boolean run(byte[] script, Transaction tx, int inputIndex) {
		Stack<byte[]> stack = new Stack<>();
		for (int i = 0; i < script.length; i++) {
			byte scriptCodeValue = ByteUtil.getByte(script, i);
			ScriptOperation scriptOperation = scriptOperationFactory.createScriptOperation(ScriptCode.safeValueOf(scriptCodeValue));
			boolean successful = scriptOperation.run(stack, tx, inputIndex);
			if (!successful) {
				return false;
			}
		}

		if (stack.size() != 1) {
			return false;
		} else {
			byte[] topValue = stack.pop();
			if (Arrays.equals(topValue, successfulFinalResult)) {
				return true;
			} else {
				return false;
			}
		}
	}
}
