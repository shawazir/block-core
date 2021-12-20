package com.example.blockchain.domain.script.operation;

import java.security.PublicKey;
import java.util.Stack;

import com.example.blockchain.domain.address.Address;
import com.example.blockchain.domain.transaction.Transaction;
import com.example.blockchain.util.PublicPrivateKeysUtil;

public class Hash160ScriptOperation implements ScriptOperation {

	@Override
	public boolean run(Stack<byte[]> stack, Transaction tx, int inputIndex) {
		byte[] encodedPublicKey = stack.peek();
		PublicKey publicKey = PublicPrivateKeysUtil.generatePublicKey(encodedPublicKey);
		Address address = new Address(publicKey);
		stack.push(address.getRipeMD160Hash());
		return true;
	}
}
