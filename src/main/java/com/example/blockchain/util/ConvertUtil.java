package com.example.blockchain.util;

public class ConvertUtil {

	// Non usable constructor
	private ConvertUtil() {

	}

	/**
	 * Converts Hex string to byte array.
	 *
	 */
	public static byte[] hexStringToByteArray(String str) {
		byte[] data = new byte[str.length() / 2];
		for (int i = 0; i < str.length(); i += 2) {
			data[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
		}
		return data;
	}

	/**
	 * Converts byte array to Hex string.
	 *
	 */
	public static String byteArrayToHexString(byte[] array) {
		char[] hexDigitsArray = "0123456789abcdef".toCharArray();
		char[] hexChars = new char[array.length * 2];
		for (int i = 0; i < array.length; i++) {
			int value = array[i] & 0xFF;
			hexChars[i * 2] = hexDigitsArray[value >>> 4];
			hexChars[i * 2 + 1] = hexDigitsArray[value & 0x0F];
		}
		return new String(hexChars);
	}
}
