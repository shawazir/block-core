package com.example.blockchain.difficulty;

import java.math.BigInteger;

import com.example.blockchain.domain.block.Bits;
import com.example.blockchain.util.ConvertUtil;

public class Target {

	public static BigInteger MAX_TARGET_VALUE = new BigInteger("00000000ffff0000000000000000000000000000000000000000000000000000", 16);

	private BigInteger value;

	public Target(BigInteger value) {
		if (validateTargetValue(value)) {
			this.value = value;
		} else {
			throw new IllegalArgumentException("Invalid bits value");
		}
	}

	public static void main(String[] args) {
		byte[] a = ConvertUtil.hexStringToByteArray(
				"3045022100fd4e6fc0dc58fd38bfa4531a2e07f775caff46313bed07849dff9ec33b2df7eb02203c8ceb2d17720cb835db789a7630f18c19237f3efd16233f4b088833c400f3ad");
		for (int i = 0; i < a.length; i++) {
			System.out.print(a[i] + ", ");
		}
	}

	public Target(Bits bits) {
		this.value = bits.toTargetValue();
	}

	/**
	 * Validates the given target value.
	 * 
	 */
	public static boolean validateTargetValue(BigInteger target) {
		if (target == null) {
			return false;
		} else if (target.compareTo(Target.MAX_TARGET_VALUE) == 1) {
			return false;
		} else if (target.compareTo(BigInteger.ONE) == -1) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Converts to Bits.
	 * 
	 */
	public Bits toBits() {
		return new Bits(value);
	}

	// GETTERS & SETTERS // -----------------------------------------

	public BigInteger getValue() {
		return value;
	}
}
