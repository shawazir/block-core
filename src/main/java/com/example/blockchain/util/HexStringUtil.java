package com.example.blockchain.util;

import java.math.BigInteger;

public class HexStringUtil {

	private static final char[] hexDigitsArray = "0123456789abcdef".toCharArray();
	public static final BigInteger MAX_ONE_BYTE_VARINT_VALUE = BigInteger.valueOf(252);
	public static final BigInteger MAX_TWO_BYTE_VARINT_VALUE = new BigInteger("FFFF", 16);
	public static final BigInteger MAX_FOUR_BYTE_VARINT_VALUE = new BigInteger("FFFFFFFF", 16);
	public static final BigInteger MAX_VARINT_VALUE = new BigInteger("FFFFFFFFFFFFFFFF", 16);
	public static final byte FD_BYTE = -3;
	public static final byte FE_BYTE = -2;
	public static final byte FF_BYTE = -1;

	// Non usable constructor
	private HexStringUtil() {

	}

	/**
	 * Validates the given hex string is valid.
	 * 
	 */
	public static boolean validateHexString(String str) {
		for (int i = 0; i < str.length(); i++) {
			for (int j = 0; j < hexDigitsArray.length; j++) {
				if (str.charAt(i) == hexDigitsArray[j]) {
					break;
				} else if (str.charAt(i) != hexDigitsArray[j] && j == hexDigitsArray.length - 1) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Extracts the string which corresponds to the specified bytes.
	 *
	 */
	public static String extractBytes(String str, int start, int length) {
		int startIndex = start * 2;
		int endIndex = startIndex + (length * 2);
		return str.substring(startIndex, endIndex);
	}

	/**
	 * Converts the given Number to a Hex value string.
	 *
	 */
	public static String convertNumberToHexValue(Number num) {
		return convertNumberToHexValue(num, 0);
	}

	/**
	 * Converts the given Number to a Hex value string.
	 *
	 */
	public static String convertNumberToHexValue(Number num, int totalLength) {
		String numStr = Long.toString(num.longValue(), 16);
		if (totalLength <= numStr.length()) {
			return numStr;
		} else {
			return StringUtil.padNumber(numStr, totalLength);
		}
	}

	/**
	 * Gets the length of VarInt of the given string. The string should start with
	 * the VarInt.
	 * 
	 */
	public static int getLengthOfVarIntString(String str) {
		return getLengthOfVarIntString(str, 0);
	}

	/**
	 * Gets the length of VarInt of the given string starting the given index.
	 * 
	 */
	// FIXME The VarInt size could go up to 8 unsigned bytes. So, the int return
	// type is not big enough to contain it.
	public static int getLengthOfVarIntString(String str, int startIndex) {
		String firstByte = str.substring(startIndex, startIndex + 2).toLowerCase();
		if (firstByte.equals("fd")) {
			return 3;
		} else if (firstByte.equals("fe")) {
			return 5;
		} else if (firstByte.equals("ff")) {
			return 9;
		} else {
			return 1;
		}
	}

	/**
	 * Gets the length of VarInt of the given byte array. The array should start
	 * with the VarInt.
	 * 
	 */
	public static int getLengthOfVarInt(byte[] data) {
		return getLengthOfVarInt(data, 0);
	}

	/**
	 * Gets the length of VarInt of the given byte array starting the given index.
	 * 
	 */
	// FIXME The VarInt size could go up to 8 unsigned bytes. So, the int return
	// type is not big enough to contain it.
	public static int getLengthOfVarInt(byte[] data, int startIndex) {
		byte firstByte = data[startIndex];
		if (firstByte == FD_BYTE) { // firstByte == 0xfd
			return 3;
		} else if (firstByte == FE_BYTE) { // firstByte == 0xfe
			return 5;
		} else if (firstByte == FF_BYTE) { // firstByte == 0xff
			return 9;
		} else {
			return 1;
		}
	}

	/**
	 * Builds a VarInt string with the given value.
	 * 
	 */
	public static String buildVarIntString(int value) {
		return buildVarIntString(BigInteger.valueOf(value));
	}

	/**
	 * Builds a VarInt string with the given value.
	 * 
	 */
	public static String buildVarIntString(long value) {
		return buildVarIntString(BigInteger.valueOf(value));
	}

	/**
	 * Builds a VarInt string with the given value.
	 * 
	 */
	public static String buildVarIntString(BigInteger value) {
		if (value.compareTo(MAX_VARINT_VALUE) > 0) {
			throw new RuntimeException("Too large value");
		}

		if (value.compareTo(MAX_ONE_BYTE_VARINT_VALUE) <= 0) {
			return StringUtil.padNumber(value.toString(16), 2);
		} else if (value.compareTo(MAX_TWO_BYTE_VARINT_VALUE) <= 0) {
			return "fd" + StringUtil.padNumber(value.toString(16), 4);
		} else if (value.compareTo(MAX_FOUR_BYTE_VARINT_VALUE) <= 0) {
			return "fe" + StringUtil.padNumber(value.toString(16), 8);
		} else {
			return "ff" + StringUtil.padNumber(value.toString(16), 16);
		}
	}

	/**
	 * Builds a VarInt byte array with the given value.
	 * 
	 */
	public static byte[] buildVarInt(int value) {
		return buildVarInt(BigInteger.valueOf(value));
	}

	/**
	 * Builds a VarInt byte array with the given value.
	 * 
	 */
	public static byte[] buildVarInt(long value) {
		return buildVarInt(BigInteger.valueOf(value));
	}

	/**
	 * Builds a VarInt byte array with the given value.
	 * 
	 */
	public static byte[] buildVarInt(BigInteger value) {
		if (value.compareTo(MAX_VARINT_VALUE) > 0) {
			throw new RuntimeException("Too large value");
		}

		if (value.compareTo(MAX_ONE_BYTE_VARINT_VALUE) <= 0) {
			return new byte[] { ByteUtil.unsignedNumberToByte(value.shortValue()) };
		} else if (value.compareTo(MAX_TWO_BYTE_VARINT_VALUE) <= 0) {
			byte[] valueInBytes = ByteUtil.convertToByteArray(value, 2);
			byte[] result = new byte[3];
			result[0] = ByteUtil.unsignedHexStringToByte("fd");
			System.arraycopy(valueInBytes, 0, result, 1, 2);
			return result;
		} else if (value.compareTo(MAX_FOUR_BYTE_VARINT_VALUE) <= 0) {
			byte[] valueInBytes = ByteUtil.convertToByteArray(value, 4);
			byte[] result = new byte[5];
			result[0] = ByteUtil.unsignedHexStringToByte("fe");
			System.arraycopy(valueInBytes, 0, result, 1, 4);
			return result;
		} else {
			byte[] valueInBytes = ByteUtil.convertToByteArray(value, 8);
			byte[] result = new byte[9];
			result[0] = ByteUtil.unsignedHexStringToByte("ff");
			System.arraycopy(valueInBytes, 0, result, 1, 8);
			return result;
		}
	}

	/**
	 * Parses the given VarInt string.
	 * 
	 */
	public static BigInteger parseVarIntString(String str) {
		str = str.toLowerCase();
		if (str.length() == 2) { // One byte value
			if (str.equals("fd") || str.equals("fe") || str.equals("ff")) {
				throw new RuntimeException("Invalid VarInt value");
			} else {
				return new BigInteger(str, 16);
			}
		} else if (str.length() == 6) { // Two byte value
			if (!str.startsWith("fd")) {
				throw new RuntimeException("Invalid VarInt value");
			} else {
				return new BigInteger(extractBytes(str, 1, 2), 16);
			}
		} else if (str.length() == 10) { // Four byte value
			if (!str.startsWith("fe")) {
				throw new RuntimeException("Invalid VarInt value");
			} else {
				return new BigInteger(extractBytes(str, 1, 4), 16);
			}
		} else if (str.length() == 18) { // Eight byte value
			if (!str.startsWith("ff")) {
				throw new RuntimeException("Invalid VarInt value");
			} else {
				return new BigInteger(extractBytes(str, 1, 8), 16);
			}
		} else {
			throw new RuntimeException("Invalid VarInt value");
		}
	}

	/**
	 * Parses the given VarInt.
	 * 
	 */
	public static BigInteger parseVarInt(byte[] data) {
		if (data.length == 1) { // One byte value
			if (data[0] == FD_BYTE || data[0] == FE_BYTE || data[0] == FF_BYTE) {
				throw new RuntimeException("Invalid VarInt value");
			} else {
				return BigInteger.valueOf(data[0] & 0xff);
			}
		} else if (data.length == 3) { // Two byte value
			if (data[0] != FD_BYTE) {
				throw new RuntimeException("Invalid VarInt value");
			} else {
				return ByteUtil.getBigInteger(data, 1, 2);
			}
		} else if (data.length == 5) { // Four byte value
			if (data[0] != FE_BYTE) {
				throw new RuntimeException("Invalid VarInt value");
			} else {
				return ByteUtil.getBigInteger(data, 1, 4);
			}
		} else if (data.length == 9) { // Eight byte value
			if (data[0] != FF_BYTE) {
				throw new RuntimeException("Invalid VarInt value");
			} else {
				return ByteUtil.getBigInteger(data, 1, 8);
			}
		} else {
			throw new RuntimeException("Invalid VarInt value");
		}
	}
}
