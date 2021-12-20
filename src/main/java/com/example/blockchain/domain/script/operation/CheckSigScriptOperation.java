package com.example.blockchain.domain.script.operation;

import java.security.PublicKey;
import java.util.Stack;

import com.example.blockchain.domain.transaction.Transaction;
import com.example.blockchain.util.ConvertUtil;
import com.example.blockchain.util.PublicPrivateKeysUtil;

public class CheckSigScriptOperation implements ScriptOperation {

	@Override
	public boolean run(Stack<byte[]> stack, Transaction tx, int inputIndex) {
		PublicKey publicKey = PublicPrivateKeysUtil.generatePublicKey(stack.pop());
		byte[] signature = stack.pop();
		byte[] originalDataOfSignature = buildOriginalDataOfSignature(tx, inputIndex);
		boolean signatureIsValid = PublicPrivateKeysUtil.verifySignedData(originalDataOfSignature, signature, publicKey);
		if (signatureIsValid) {
			stack.push(new byte[] { 1 });
			return true;
		} else {
			stack.push(new byte[] { 0 });
			return true;
		}
	}

	private byte[] buildOriginalDataOfSignature(Transaction tx, int inputIndex) {
		int outputIndex = tx.getInputs().get(inputIndex).getOutputIndex();
		byte[] previousScriptPubKey = tx.getInputs().get(inputIndex).getTransaction().getOutputs().get(outputIndex).getScriptPubKey();
		ConvertUtil.byteArrayToHexString(previousScriptPubKey);
		Transaction clonedTx = Transaction.buildTransaction(tx.serialize());
		for (int i = 0; i < clonedTx.getInputs().size(); i++) {
			if (i == inputIndex) {
				clonedTx.getInputs().get(i).setScriptSig(previousScriptPubKey);
			} else {
				clonedTx.getInputs().get(i).setScriptSig(new byte[0]);
			}
		}
		ConvertUtil.byteArrayToHexString(clonedTx.serialize());
		ConvertUtil.byteArrayToHexString(
				PublicPrivateKeysUtil.signData(clonedTx.serialize(), PublicPrivateKeysUtil.generatePrivateKey(ConvertUtil.hexStringToByteArray(
						"30818d020100301006072a8648ce3d020106052b8104000a047630740201010420397d5c0c211709543b97477866cbf75d3faf43b6254cc40a7078e7eb69a46bc1a00706052b8104000aa1440342000462d5838f86c2a2b64570e3e604943dcfca0be3f9f645c581fe6679c2c1b59e3dca62a3490107a62d609e7235ae715c1f3f64d371a5df84e303e5b74e50f144d9"))));
		return clonedTx.serialize();
	}
}
