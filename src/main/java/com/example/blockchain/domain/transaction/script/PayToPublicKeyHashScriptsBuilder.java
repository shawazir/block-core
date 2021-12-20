package com.example.blockchain.domain.transaction.script;

import java.security.PublicKey;

import com.example.blockchain.domain.address.Address;
import com.example.blockchain.domain.script.ScriptCode;

public class PayToPublicKeyHashScriptsBuilder {

	public byte[] buildScriptSig(byte[] signature, PublicKey publicKey) {
		byte[] encodedPublicKey = publicKey.getEncoded();
		byte[] scriptSig = new byte[signature.length + encodedPublicKey.length];
		System.arraycopy(signature, 0, scriptSig, 0, signature.length);
		System.arraycopy(encodedPublicKey, 0, scriptSig, signature.length, encodedPublicKey.length);
		return scriptSig;
	}

	public byte[] buildScriptPubKey(Address address) {
		byte[] ripeMD160Hash = address.getRipeMD160Hash();
		byte[] scriptPubKey = new byte[ripeMD160Hash.length + 4];
		scriptPubKey[0] = ScriptCode.DUP.getValue();
		scriptPubKey[1] = ScriptCode.HASH160.getValue();
		System.arraycopy(ripeMD160Hash, 0, scriptPubKey, 2, ripeMD160Hash.length);
		scriptPubKey[scriptPubKey.length - 2] = ScriptCode.EQUALVERIFY.getValue();
		scriptPubKey[scriptPubKey.length - 1] = ScriptCode.CHECKSIG.getValue();
		return scriptPubKey;
	}
}
