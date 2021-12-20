package com.example.blockchain.domain.address;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPoint;

import org.bitcoinj.core.Base58;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.example.blockchain.util.ConvertUtil;
import com.example.blockchain.util.Sha256Util;
import com.example.blockchain.util.StringUtil;

public class Address {

	private static MessageDigest ripeMD160MessageDigest;

	static {
		Security.addProvider(new BouncyCastleProvider());
		try {
			ripeMD160MessageDigest = MessageDigest.getInstance("RipeMD160", "BC");
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			throw new RuntimeException(e);
		}
	}

	private byte[] hash160Address;
	private String base58Address;

	public Address(String base58Address) {
		boolean base58AddressIsValid = validateBase58Address(base58Address);
		if (base58AddressIsValid) {
			this.base58Address = base58Address;
			this.hash160Address = convertBase58AddressToHash160Address(base58Address);
		} else {
			throw new RuntimeException("Invalid address");
		}
	}

	public Address(PublicKey publicKey) {
		generateAddress(publicKey);
	}

	public byte[] getRipeMD160Hash() {
		return hash160Address;
	}

	public String getRipeMD160HashAsString() {
		return ConvertUtil.byteArrayToHexString(hash160Address);
	}

	public String getBase58Address() {
		return this.base58Address;
	}

	/**
	 * Generates the address from the given public key.
	 * https://www.novixys.com/blog/generate-bitcoin-addresses-java
	 * 
	 */
	private void generateAddress(PublicKey publicKey) {
		// Builds the public key string
		ECPublicKey epub = (ECPublicKey) publicKey;
		ECPoint ecPoint = epub.getW();
		String sx = StringUtil.padNumber(ecPoint.getAffineX().toString(16), 64);
		String sy = StringUtil.padNumber(ecPoint.getAffineY().toString(16), 64);
		String publicKeyStr = "04" + sx + sy;

		// Hashes the public key string with SHA-256
		byte[] hashedPublicKey = Sha256Util.hash(ConvertUtil.hexStringToByteArray(publicKeyStr));

		// Hashes the public key string with RipeMD-160
		byte[] ripeMD160Hash = ripeMD160MessageDigest.digest(hashedPublicKey);

		// Adds a 0x00 version byte at the beginning
		byte[] extendedRipeMD160Hash = new byte[ripeMD160Hash.length + 1];
		extendedRipeMD160Hash[0] = 0;
		for (int i = 0; i < ripeMD160Hash.length; i++) {
			extendedRipeMD160Hash[i + 1] = ripeMD160Hash[i];
		}

		// Hashes the RipeMD-160 hash twice
		byte[] doubleHashed160 = Sha256Util.doubleHash(extendedRipeMD160Hash);

		// Append the first four bytes to the extended RipeMD-160 hash as checksum
		byte[] extendedRipeMD160HashWithChecksum = new byte[25];
		for (int i = 0; i < extendedRipeMD160Hash.length; i++) {
			extendedRipeMD160HashWithChecksum[i] = extendedRipeMD160Hash[i];
		}
		for (int i = 0; i < 4; i++) {
			extendedRipeMD160HashWithChecksum[21 + i] = doubleHashed160[i];
		}

		this.hash160Address = extendedRipeMD160HashWithChecksum;

		// Encodes the address to Base58
		String address = Base58.encode(extendedRipeMD160HashWithChecksum);
		this.base58Address = address;
	}

	private byte[] convertBase58AddressToHash160Address(String base58Address) {
		byte[] base58DecodedAddressByteArray = Base58.decode(base58Address);
		return base58DecodedAddressByteArray;
	}

	/**
	 * Validates the given address.
	 *
	 */
	public static boolean validateHash160Address(byte[] address) {
		if (address.length != 25) {
			return false;
		}

		String base58DecodedAddressString = ConvertUtil.byteArrayToHexString(address);
		String extendedRipeMD160Hash = base58DecodedAddressString.substring(0, 42);
		String checksum = base58DecodedAddressString.substring(42);
		String doubleHash = Sha256Util.doubleHashToString(ConvertUtil.hexStringToByteArray(extendedRipeMD160Hash));
		String firstFourBytesOfDoubleHash = doubleHash.substring(0, 8);
		if (firstFourBytesOfDoubleHash.equals(checksum)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Validates the given address.
	 *
	 */
	public static boolean validateHash160Address(String address) {
		if (address == null || address.length() != 50) {
			return false;
		}

		byte[] byteArray = Base58.decode(address);
		return validateHash160Address(byteArray);
	}

	/**
	 * Validates the given address.
	 *
	 */
	public static boolean validateBase58Address(String address) {
		if (address == null || address.length() != 34) {
			return false;
		}

		byte[] base58DecodedAddressByteArray = Base58.decode(address);
		return validateHash160Address(base58DecodedAddressByteArray);
	}

	@Override
	public String toString() {
		return base58Address;
	}
}
