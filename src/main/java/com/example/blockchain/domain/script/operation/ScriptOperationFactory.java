package com.example.blockchain.domain.script.operation;

import com.example.blockchain.domain.script.ScriptCode;

public class ScriptOperationFactory {

	public ScriptOperation createScriptOperation(ScriptCode scriptCode) {
		if (scriptCode == ScriptCode.DUP) {
			return new DupScriptOperation();
		} else if (scriptCode == ScriptCode.EQUALVERIFY) {
			return new EqualVerifyScriptOperation();
		} else if (scriptCode == ScriptCode.HASH160) {
			return new Hash160ScriptOperation();
		} else if (scriptCode == ScriptCode.CHECKSIG) {
			return new CheckSigScriptOperation();
		} else {
			throw new RuntimeException("ScriptCode not Identified by Factory");
		}
	}
}
