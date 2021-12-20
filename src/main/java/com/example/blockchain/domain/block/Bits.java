package com.example.blockchain.domain.block;

import java.math.BigInteger;
import java.util.Arrays;

import com.example.blockchain.difficulty.Target;
import com.example.blockchain.domain.block.builder.BlockBuilderV1;
import com.example.blockchain.util.ByteUtil;
import com.example.blockchain.util.ConvertUtil;

public class Bits {

	private byte[] value;

	public Bits(byte[] value) {
		if (validateBitsValue(value)) {
			this.value = value;
		} else {
			throw new IllegalArgumentException("Invalid bits value");
		}
	}

	public Bits(BigInteger target) {
		if (!Target.validateTargetValue(target)) {
			throw new IllegalArgumentException("Invalid target value");
		}

		byte[] targetByteArray = target.toByteArray();
		byte[] exponent = ByteUtil.convertToByteArray(targetByteArray.length, 1);
		byte[] coefficient = new byte[3];
		if (targetByteArray.length < 3) {
			System.arraycopy(targetByteArray, 0, coefficient, 3 - targetByteArray.length, targetByteArray.length);
		} else {
			System.arraycopy(targetByteArray, 0, coefficient, 0, 3);
		}
		this.value = new byte[BlockBuilderV1.BITS_SIZE];
		System.arraycopy(exponent, 0, this.value, 0, 1);
		System.arraycopy(coefficient, 0, this.value, 1, 3);
	}

	/**
	 * Converts the Bits instance to target value. The Target class is not returned
	 * instead because the height value is not available.
	 * 
	 */
	public BigInteger toTargetValue() {
		int exponent = ByteUtil.getInt(value, 0, 1);
		BigInteger coefficient = new BigInteger(new byte[] { value[1], value[2], value[3] });
		int shiftAmount = (exponent - 3) * 8;
		BigInteger target = coefficient.shiftLeft(shiftAmount);
		return target;
	}

	/**
	 * Validates the given bits value.
	 * 
	 */
	private boolean validateBitsValue(byte[] value) {
		if (value == null) {
			return false;
		} else if (value.length != BlockBuilderV1.BITS_SIZE) {
			return false;
		} else {
			return true;
		}
	}

	// Object METHODS // --------------------------------------------

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(value);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Bits other = (Bits) obj;
		if (!Arrays.equals(value, other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return ConvertUtil.byteArrayToHexString(value);
	}

	// GETTERS & SETTERS // -----------------------------------------

	public byte[] getValue() {
		return value;
	}
}
