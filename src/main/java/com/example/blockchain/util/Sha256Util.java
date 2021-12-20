package com.example.blockchain.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha256Util {

	// Non usable constructor
	private Sha256Util() {

	}

	/**
	 * Hashes the given byte array.
	 *
	 */
	public static byte[] hash(byte[] array) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			byte[] hash = messageDigest.digest(array);
			return hash;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256 algorith not found");
		}
	}

	/**
	 * Hashes the given byte array twice.
	 *
	 */
	public static byte[] doubleHash(byte[] array) {
		try {
			MessageDigest messageDigest1 = MessageDigest.getInstance("SHA-256");
			MessageDigest messageDigest2 = MessageDigest.getInstance("SHA-256");
			byte[] hash = messageDigest1.digest(messageDigest2.digest(array));
			return hash;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256 algorith not found");
		}
	}

	/**
	 * Hashes the given byte array to a result of type String.
	 *
	 */
	public static String hashToString(byte[] array) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			byte[] hash = messageDigest.digest(array);
			return ConvertUtil.byteArrayToHexString(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256 algorith not found");
		}
	}

	/**
	 * Hashes the given byte array to a result of type String twice.
	 *
	 */
	public static String doubleHashToString(byte[] array) {
		try {
			MessageDigest messageDigest1 = MessageDigest.getInstance("SHA-256");
			MessageDigest messageDigest2 = MessageDigest.getInstance("SHA-256");
			byte[] hash = messageDigest1.digest(messageDigest2.digest(array));
			return ConvertUtil.byteArrayToHexString(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256 algorith not found");
		}
	}
}
