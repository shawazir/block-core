package com.example.blockchain.util;

import java.math.BigInteger;

public class ByteUtil {

	// Non usable constructor
	private ByteUtil() {

	}

	public static byte getByte(byte[] array, int index) {
		return array[index];
	}

	public static short getShort(byte[] array, int index, int length) {
		int counter = 0;
		short value = 0;
		while (counter < length) {
			value = (short) ((value << 8) + (array[index] & 0xff));
			index++;
			counter++;
		}
		return value;
	}

	public static int getInt(byte[] array, int index, int length) {
		int counter = 0;
		long value = 0;
		while (counter < length) {
			value = (value << 8) + (array[index] & 0xff);
			index++;
			counter++;
		}
		return (int) value;
	}

	public static long getLong(byte[] array, int index, int length) {
		int counter = 0;
		long value = 0;
		while (counter < length) {
			value = (long) ((value << 8) + (array[index] & 0xff));
			index++;
			counter++;
		}
		return value;
	}

	public static BigInteger getBigInteger(byte[] array, int index, int length) {
		int counter = 0;
		BigInteger value = BigInteger.ZERO;
		while (counter < length) {
			value = value.shiftLeft(8).add(BigInteger.valueOf(array[index] & 0xff));
			index++;
			counter++;
		}
		return value;
	}

	public static byte[] getBytes(byte[] array, int startIndex, int length) {
		byte[] bytes = new byte[length];
		for (int i = 0; i < length; i++) {
			bytes[i] = array[startIndex + i];
		}
		return bytes;
	}

	public static byte[] cloneByteArray(byte[] array) {
		if (array == null) {
			throw new IllegalArgumentException("The array to be cloned cannot be null");
		} else if (array.length == 0) {
			return new byte[0];
		}

		byte[] cloned = new byte[array.length];
		for (int i = 0; i < array.length; i++) {
			cloned[i] = array[i];
		}
		return cloned;
	}

	public static byte[] convertToByteArray(long num, int arrayLength) {
		BigInteger bigInteger = BigInteger.valueOf(num);
		return convertToByteArray(bigInteger, arrayLength);
	}

	/**
	 * Converts the given number to a byte array in big-endian with length as the
	 * given arrayLength . If the given arrayLength is -1, that's treated as if no
	 * length is specified. Hence, the array is returned with it's original length.
	 * 
	 */
	public static byte[] convertToByteArray(BigInteger num, int arrayLength) {
		byte[] array = num.toByteArray();
		if (arrayLength == -1) {
			return array;
		} else {
			byte[] result = new byte[arrayLength];
			if (array.length > arrayLength) {
				System.arraycopy(array, array.length - arrayLength, result, 0, arrayLength);
			} else {
				System.arraycopy(array, 0, result, arrayLength - array.length, array.length);
			}
			return result;
		}
	}

	/**
	 * Converts the given hex String to a byte as if it's unsigned. This method
	 * tries to compensate for Java's lack of unsigned data types and, hence,
	 * provides this workaround. The resulting value should be perceived as unsigned
	 * although internally Java treats it as signed.
	 * 
	 */
	public static byte unsignedHexStringToByte(String hex) {
		if (hex.length() > 2) {
			throw new RuntimeException("Value too large for byte data type");
		}

		short num = Short.parseShort(hex, 16);
		return unsignedNumberToByte(num);
	}

	/**
	 * Converts the given number to a byte as if it's unsigned. This method tries to
	 * compensate for Java's lack of unsigned data types and, hence, provides this
	 * workaround. The resulting value should be perceived as unsigned although
	 * internally Java treats it as signed.
	 * 
	 */
	public static byte unsignedNumberToByte(short num) {
		if (num > 255) {
			throw new RuntimeException("Value too large for byte data type");
		}

		byte b;
		if (num < 128) {
			b = (byte) num;
		} else {
			b = (byte) (num - 256);
		}
		return b;
	}

	public static boolean isArraysEqual(byte[] array1, byte[] array2) {
		if (array1 == null && array2 == null) {
			return true;
		} else if (array1 == null || array2 == null || array1.length != array2.length) {
			return false;
		} else if (array1.length == 0 && array2.length == 0) {
			return true;
		}

		for (int i = 0; i < array1.length; i++) {
			if (array1[i] != array2[i]) {
				return false;
			}
		}

		return true;
	}

	public static boolean contains(byte[] arrayToLookIn, byte[] arrayToLookFor) {
		if (arrayToLookFor == null || arrayToLookIn == null || arrayToLookFor.length == 0 || arrayToLookIn.length == 0) {
			return false;
		} else if (arrayToLookFor.length > arrayToLookIn.length) {
			return false;
		}

		// 1 2 3 4 5
		// 2 3 4

		for (int i = 0; i < arrayToLookIn.length; i++) {
			boolean remainingArrayIsTooShort = arrayToLookIn.length - i < arrayToLookFor.length;
			if (remainingArrayIsTooShort) {
				return false;
			}

			int cursor = 0;
			boolean match = false;
			for (int j = 0; j < arrayToLookFor.length; j++) {
				if (arrayToLookIn[i + cursor] == arrayToLookFor[j]) {
					match = true;
					cursor++;
				} else {
					match = false;
					break;
				}
			}

			if (match) {
				return true;
			}
		}
		return false;
	}
}
