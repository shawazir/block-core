package com.example.blockchain.util;

import org.bouncycastle.util.Arrays;

public class ScriptCodesUtil {

	static final int MIN_ONE_BYTE_SIZE = 76;
	static final int MAX_ONE_BYTE_SIZE = Integer.parseInt("ff", 16);
	static final int MIN_TWO_BYTE_SIZE = Integer.parseInt("100", 16);
	static final int MAX_TWO_BYTE_SIZE = Integer.parseInt("ffff", 16);
	static final int MIN_FOUR_BYTE_SIZE = Integer.parseInt("1000000", 16);
	static final byte BYTE_4C = 76;
	static final byte BYTE_4D = 77;
	static final byte BYTE_4E = 78;

	// Non usable constructor
	private ScriptCodesUtil() {

	}

	/**
	 * Generates the PUSH codes for the given value.
	 *
	 */
	private static byte[] getPushCodeForSize(int size) {
		if (size < 0) {
			throw new IllegalArgumentException();
		}

		if (size == 0) {
			return new byte[0];
		} else if (size >= 1 && size < MIN_ONE_BYTE_SIZE) {
			return ByteUtil.convertToByteArray(size, 1);
		} else if (size >= MIN_ONE_BYTE_SIZE && size <= MAX_ONE_BYTE_SIZE) {
			return Arrays.concatenate(new byte[] { BYTE_4C }, ByteUtil.convertToByteArray(size, 1));
		} else if (size >= MIN_TWO_BYTE_SIZE && size <= MAX_TWO_BYTE_SIZE) {
			return Arrays.concatenate(new byte[] { BYTE_4D }, ByteUtil.convertToByteArray(size, 2));
		} else if (size >= MIN_FOUR_BYTE_SIZE && size <= Integer.MAX_VALUE) {
			return Arrays.concatenate(new byte[] { BYTE_4E }, ByteUtil.convertToByteArray(size, 4));
		} else {
			throw new IllegalArgumentException("Too long string");
		}
	}

	/**
	 * Generates the PUSH codes for the given value.
	 *
	 */
	public static byte[] getPushCodeForTextValue(String str, boolean includeValue) {
		if (str == null) {
			throw new IllegalArgumentException();
		}

		byte[] pushCode = getPushCodeForSize(str.length());
		if (includeValue) {
			return Arrays.concatenate(pushCode, str.getBytes());
		} else {
			return pushCode;
		}
	}

	/**
	 * Generates the PUSH codes for the given value.
	 *
	 */
	public static byte[] getPushCodeForTextValue(String str) {
		return getPushCodeForTextValue(str, false);
	}

	/**
	 * Generates the PUSH codes for the given value.
	 *
	 */
	public static byte[] getPushCodeForNumericalValue(int num, boolean includeValue) {
		if (num < 0) {
			throw new IllegalArgumentException();
		}

		byte[] numBytes = ByteUtil.convertToByteArray(num, -1);
		byte[] pushCode = getPushCodeForSize(numBytes.length);
		if (includeValue) {
			return Arrays.concatenate(pushCode, numBytes);
		} else {
			return pushCode;
		}
	}

	/**
	 * Generates the PUSH codes for the given value.
	 *
	 */
	public static byte[] getPushCodeForNumericalValue(int num) {
		return getPushCodeForNumericalValue(num, false);
	}
}
