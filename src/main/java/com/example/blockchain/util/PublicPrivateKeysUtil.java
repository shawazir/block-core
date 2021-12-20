package com.example.blockchain.util;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

import com.example.blockchain.domain.address.Address;
import com.example.blockchain.domain.transaction.Transaction;
import com.example.blockchain.domain.transaction.builder.BuildTransactionForm;
import com.example.blockchain.domain.transaction.script.TransactionScriptsType;

public class PublicPrivateKeysUtil {

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	// Non usable constructor
	private PublicPrivateKeysUtil() {

	}

	/**
	 * Generates new random KeyPair.
	 * 
	 */
	public static KeyPair generateNewKeyPair() {
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
			keyPairGenerator.initialize(new ECGenParameterSpec("secp256k1"), new SecureRandom());
			KeyPair keyPair = keyPairGenerator.generateKeyPair();
			return keyPair;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (NoSuchProviderException e) {
			throw new RuntimeException(e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Generates the PrivateKey object of the given encoded private key.
	 * https://stackoverflow.com/questions/45968285/reconstructing-private-and-public-keys-with-bouncy-castle
	 * 
	 */
	public static PrivateKey generatePrivateKey(byte[] encodedPrivateKey) {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
			PKCS8EncodedKeySpec pKCS8EncodedKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
			PrivateKey privateKey = keyFactory.generatePrivate(pKCS8EncodedKeySpec);
			return privateKey;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (NoSuchProviderException e) {
			throw new RuntimeException(e);
		} catch (InvalidKeySpecException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Generates the PublicKey object of the given encoded public key.
	 * https://stackoverflow.com/questions/45968285/reconstructing-private-and-public-keys-with-bouncy-castle
	 * 
	 */
	public static PublicKey generatePublicKey(byte[] encodedPublicKey) {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
			X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(encodedPublicKey);
			PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
			return publicKey;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (NoSuchProviderException e) {
			throw new RuntimeException(e);
		} catch (InvalidKeySpecException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Generates the KeyPair object of the given encoded private and public keys.
	 * 
	 */
	public static KeyPair generateKeyPair(byte[] encodedPrivateKey, byte[] encodedPublicKey) {
		PublicKey publicKey = generatePublicKey(encodedPublicKey);
		PrivateKey privateKey = generatePrivateKey(encodedPrivateKey);
		return new KeyPair(publicKey, privateKey);
	}

	/**
	 * Gets the public key from the given private key.
	 * https://stackoverflow.com/questions/49204787/deriving-ecdsa-public-key-from-private-key
	 *
	 */
	public static PublicKey getPublicKeyFromPrivateKey(PrivateKey privateKey) {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
			ECParameterSpec eCParameterSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
			ECPoint q = eCParameterSpec.getG().multiply(((org.bouncycastle.jce.interfaces.ECPrivateKey) privateKey).getD());
			ECPublicKeySpec eCPublicKeySpec = new ECPublicKeySpec(q, eCParameterSpec);
			PublicKey publicKey = keyFactory.generatePublic(eCPublicKeySpec);
			return publicKey;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (NoSuchProviderException e) {
			throw new RuntimeException(e);
		} catch (InvalidKeySpecException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		String encodedPrivateKeyStr = "30818d020100301006072a8648ce3d020106052b8104000a047630740201010420397d5c0c211709543b97477866cbf75d3faf43b6254cc40a7078e7eb69a46bc1a00706052b8104000aa1440342000462d5838f86c2a2b64570e3e604943dcfca0be3f9f645c581fe6679c2c1b59e3dca62a3490107a62d609e7235ae715c1f3f64d371a5df84e303e5b74e50f144d9";
		String encodedPublicKeyStr = "3056301006072a8648ce3d020106052b8104000a0342000462d5838f86c2a2b64570e3e604943dcfca0be3f9f645c581fe6679c2c1b59e3dca62a3490107a62d609e7235ae715c1f3f64d371a5df84e303e5b74e50f144d9";

		byte[] encodedPrivateKey = ConvertUtil.hexStringToByteArray(encodedPrivateKeyStr);
		byte[] encodedPublicKey = ConvertUtil.hexStringToByteArray(encodedPublicKeyStr);

		TransactionScriptsType scriptsType = TransactionScriptsType.PAY_TO_PUBLIC_KEY;
		Transaction tx1 = new Transaction(ConvertUtil.hexStringToByteArray("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"));
		Transaction tx2 = new Transaction(ConvertUtil.hexStringToByteArray("486ea46224d1bb4fb680f34f7c9ad96a8f24ec88be73ea8e5a6c65260e9cb8a7"));
		KeyPair unlockingKeyPair = generateKeyPair(encodedPrivateKey, encodedPublicKey);
		BigInteger[] amounts = new BigInteger[] { new BigInteger("100000000"), new BigInteger("200000000") };
		PublicKey[] lockingPublicKeys = new PublicKey[] { generatePublicKey(encodedPublicKey), generatePublicKey(encodedPublicKey) };
		Address[] lockingAddresses = new Address[] { new Address(generatePublicKey(encodedPublicKey)), new Address(generatePublicKey(encodedPublicKey)) };
		long locktime = 4294967295L;
		BuildTransactionForm buildTransactionForm = new BuildTransactionForm(scriptsType, new Transaction[] { tx1, tx2 }, new int[] { 0, 1 },
				new KeyPair[] { unlockingKeyPair, unlockingKeyPair }, amounts, lockingPublicKeys, lockingAddresses, locktime);
		Transaction tx = Transaction.createTransaction(buildTransactionForm);
		System.out.println(tx.serialize());
	}

	/**
	 * Signs the given text with the given PrivateKey.
	 * https://stackoverflow.com/questions/7224626/how-to-sign-string-with-private-key
	 *
	 */
	public static byte[] signData(byte[] data, PrivateKey privateKey) {
		try {
			Signature signature = Signature.getInstance("SHA256withECDSA", "BC");
			signature.initSign(privateKey);
			signature.update(data);
			byte[] signatureBytes = signature.sign();
			return signatureBytes;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (NoSuchProviderException e) {
			throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		} catch (SignatureException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Verifies the given text and signed text with the given PublicKey.
	 * https://stackoverflow.com/questions/7224626/how-to-sign-string-with-private-key
	 *
	 */
	public static boolean verifySignedData(byte[] data, byte[] signedData, PublicKey publicKey) {
		try {
			Signature signature = Signature.getInstance("SHA256withECDSA", "BC");
			signature.initVerify(publicKey);
			signature.update(data);
			return signature.verify(signedData);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (NoSuchProviderException e) {
			throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		} catch (SignatureException e) {
			throw new RuntimeException(e);
		}
	}
}
