package com.example.blockchain.domain.script;

import com.example.blockchain.util.ByteUtil;
import com.example.blockchain.util.ConvertUtil;

public enum ScriptCode {

	ZERO("00"), PUSHDATA1("4c"), PUSHDATA2("4d"), PUSHDATA4("4e"), NEGATE1("4f"), ONE("51"), DUP("76"), EQUALVERIFY("88"), HASH160("a9"), CHECKSIG("ac");

	private byte value;

	private ScriptCode(String value) {
		this.value = ByteUtil.unsignedHexStringToByte(value);
	}

	public byte getValue() {
		return value;
	}

	public static ScriptCode safeValueOf(byte value) {
		for (ScriptCode scriptCode : ScriptCode.values()) {
			if (scriptCode.getValue() == value) {
				return scriptCode;
			}
		}
		return null;
	}

	public String toString() {
		return ConvertUtil.byteArrayToHexString(new byte[] { value });
	}
}
