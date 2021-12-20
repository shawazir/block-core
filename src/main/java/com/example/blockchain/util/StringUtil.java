package com.example.blockchain.util;

public class StringUtil {

	// Non usable constructor
	private StringUtil() {

	}

	/**
	 * Pads the given number with zeros.
	 *
	 */
	public static String padNumber(int num, int paddingSize) {
		String numString = num + "";
		return padNumber(numString, paddingSize);
	}

	/**
	 * Pads the given number with zeros.
	 *
	 */
	public static String padNumber(String num, int paddingSize) {
		String numString = num + "";
		while (numString.length() < paddingSize) {
			numString = "0" + numString;
		}
		return numString;
	}
}
