package com.example.blockchain.domain.transaction.script;

import java.security.PublicKey;

import com.example.blockchain.domain.script.ScriptCode;

public class PayToPublicKeyScriptsBuilder {

	public byte[] buildScriptSig(byte[] signature) {
		return signature;
	}

	public byte[] buildScriptPubKey(PublicKey publicKey) {
		byte[] encodedPublicKey = publicKey.getEncoded();
		byte[] scriptPubKey = new byte[encodedPublicKey.length + 1];
		System.arraycopy(encodedPublicKey, 0, scriptPubKey, 0, encodedPublicKey.length);
		scriptPubKey[scriptPubKey.length - 1] = ScriptCode.CHECKSIG.getValue();
		return scriptPubKey;
	}
}
