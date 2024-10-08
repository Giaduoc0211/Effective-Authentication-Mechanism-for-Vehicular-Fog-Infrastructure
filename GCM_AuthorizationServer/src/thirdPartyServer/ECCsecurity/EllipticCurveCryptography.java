package thirdPartyServer.ECCsecurity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.modes.CCMBlockCipher;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.math.ec.ECPoint;

//import client.app.crypto.AesGcm256;
import thirdPartyServer.util.ServerConstants;

public class EllipticCurveCryptography {

	// public static final long timePoint5_3 = 0;
	private static ECPrivateKeyParameters privateKey;
	private static ECPublicKeyParameters publicKey;
	private static Map<String, ECPrivateKeyParameters> ZPrivateKeys = new HashMap<>();
	private static Map<String, ECPublicKeyParameters> clientsPublicKeys = new HashMap<>();
	private static Map<String, String> clientsIDandq = new HashMap<>();
	private static byte[] resRegRandomR;
	public static long time5_3;
	public static long time6_7;
	/*
	 * public EllipticCurveCryptography() { privateKey = null; publicKey = null; }
	 */

	/* Transform a byte array in an hexadecimal string */
	private static String toHex(byte[] data) {
		StringBuilder sb = new StringBuilder();
		for (byte b : data) {
			sb.append(String.format("%02x", b & 0xff));
		}
		return sb.toString();
	}

	/* Transform an hexadecimal string in byte array */
	private static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	/* Convert a string representation in its hexadecimal string */
	private static String toHex(String arg) {
		return String.format("%02x", new BigInteger(1, arg.getBytes()));
	}

	/* Convert hexadecimal notation in the ascii characters */
	private static String convertHexToString(String hex) {

		StringBuilder sb = new StringBuilder();
		StringBuilder temp = new StringBuilder();

		// 49204c6f7665204a617661 split into two characters 49, 20, 4c...
		for (int i = 0; i < hex.length() - 1; i += 2) {

			// grab the hex in pairs
			String output = hex.substring(i, (i + 2));
			// convert hex to decimal
			int decimal = Integer.parseInt(output, 16);
			// convert the decimal to character
			sb.append((char) decimal);

			temp.append(decimal);
		}

		return sb.toString();
	}

	/* Concatenation of two byte arrays */
	private static byte[] concatByteArrays(byte[] a, byte[] b) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			outputStream.write(a);
			outputStream.write(b);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] concatResult = outputStream.toByteArray();
		return concatResult;
	}

	/* Perform SHA256 and return the result */
	private static byte[] sha256(byte[] data) {
		SHA256Digest digest = new SHA256Digest();
		byte[] hash = new byte[digest.getDigestSize()];
		digest.update(data, 0, data.length);
		digest.doFinal(hash, 0);
		return hash;
	}

	public static void createECKeyPair() {
		// Get domain parameters for example curve secp256r1
		X9ECParameters ecp = SECNamedCurves.getByName("secp256r1");
		ECDomainParameters domainParams = new ECDomainParameters(ecp.getCurve(), ecp.getG(), ecp.getN(), ecp.getH(),
				ecp.getSeed());
		// Generate a private key and a public key
		AsymmetricCipherKeyPair keyPair;
		ECKeyGenerationParameters keyGenParams = new ECKeyGenerationParameters(domainParams, new SecureRandom());
		ECKeyPairGenerator generator = new ECKeyPairGenerator();
		generator.init(keyGenParams);
		keyPair = generator.generateKeyPair();

		privateKey = (ECPrivateKeyParameters) keyPair.getPrivate();
		publicKey = (ECPublicKeyParameters) keyPair.getPublic();

		byte[] privateKeyBytes = privateKey.getD().toByteArray();

		// First print our generated private key and public key
		System.out.println("Private key: " + toHex(privateKeyBytes));
		// System.out.println("Private key22: " + privateKey.getD());
		System.out.println("Public key: " + toHex(publicKey.getQ().getEncoded(true)));
		// System.out.println("Public key22: " + publicKey.getQ());

		// Then calculate the public key only using domainParams.getG() and private key
		/*
		 * ECPoint Q = domainParams.getG().multiply(new BigInteger(privateKeyBytes));
		 * System.out.println("Calculated public key: " + toHex(Q.getEncoded(true)));
		 * 
		 * // The calculated public key and generated public key should always match if
		 * (!toHex(publicKey.getQ().getEncoded(true)).equals(toHex(Q.getEncoded(true))))
		 * { System.out.println("ERROR: Public keys do not match!"); } else {
		 * System.out.println("Congratulations, public keys match!"); }
		 */

	}

	private static BigInteger computeUserq(byte[] clientIDbytes, byte[] cert_u, BigInteger a) {

		/* Concatenation of 2 bytes array */
		byte[] certIDconcat = concatByteArrays(cert_u, clientIDbytes);

		/* Do the sha256 of the certIDconcat byte array */
		long multi = System.nanoTime();
		byte[] hash = sha256(certIDconcat);
		System.out.println("******SHA256_5_6: " + (System.nanoTime() - multi));

		BigInteger bigIntHash = new BigInteger(hash);
		System.out.println("Hash value: " + bigIntHash);
		multi = System.nanoTime();
		BigInteger hashRandMult = bigIntHash.multiply(a);
		System.out.println("******star_multi5_6: " + (System.nanoTime() - multi));
		System.out.println("Hash multiplied value: " + hashRandMult);
		multi = System.nanoTime();
		BigInteger qUser = hashRandMult.add(privateKey.getD()); // k
		System.out.println("******star_add5_6: " + (System.nanoTime() - multi));
		System.out.println("q: " + qUser);

		// Create an hashMap to retrieve the combination clientID and q value
		clientsIDandq.put(toHex(clientIDbytes), toHex(qUser.toByteArray()));

		return qUser;
	}

	public static String ECQVRegistration(String clientID, String stringHexEncodedU) {
		time5_3 = System.nanoTime();
		System.out.println("*****************Time start 5.3***********: " + time5_3);
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");
		Date date = new Date();
		System.out.println("***************time start 5.3 BUGGG****************"+dateFormat.format(date));
		System.out.println("\n >>>>>>> Process 5.3 to 5.7 created a,A,cert_U, qu,Pu .....");
		byte[] clientIDbytes = hexStringToByteArray(clientID);
		// Get domain parameters for example curve secp256r1
		X9ECParameters ecp = SECNamedCurves.getByName("secp256r1");
		ECDomainParameters domainParams = new ECDomainParameters(ecp.getCurve(), ecp.getG(), ecp.getN(), ecp.getH(),
				ecp.getSeed());
		System.out.println("\n >>>>>>> Process 5.3 created a .....");

		SecureRandom random = new SecureRandom();
		byte[] a = new byte[ServerConstants.randomNumberSize]; // Create a byte array with a size of 32 bytes
		random.nextBytes(a); // Fill the array with random bytes
		// add
		System.out.println("a = " + toHex(a));

		System.out.println("\n >>>>>>> Process 5.4 created A=a.G .....");
		long multi = System.nanoTime();
		ECPoint pointA = domainParams.getG().multiply(new BigInteger(a));
		System.out.println("******star_multi5_4: " + (System.nanoTime() - multi));

		/* Decode the received encoded U to obtain the point U */
		byte[] encodedU = hexStringToByteArray(stringHexEncodedU);
		ECPoint pointU = ecp.getCurve().decodePoint(encodedU);

		/*
		 * Compute the client certificate with the elliptic curve point addition
		 * operation
		 */
		System.out.println("\n >>>>>>> Process 5.5 created cert_u .....");
		multi = System.nanoTime();
		ECPoint cert_u = pointU.add(pointA);
		System.out.println("******star_add5_5: " + (System.nanoTime() - multi));
		byte[] encodedCert_u = cert_u.getEncoded(true);
		System.out.println("user certificate =" + toHex(encodedCert_u));

		// Compute the q parameter
		System.out.println("\n >>>>>>> Process 5.6 created qu=H(cert_u||IDu)a + k  .....");
		BigInteger qUser = computeUserq(clientIDbytes, encodedCert_u, new BigInteger(a));
		System.out.println("qUser = " + toHex(qUser.toByteArray()));

		/* Encode the public key that needs to be sent over the http channel */
		System.out.println("\n >>>>>>> Process 5.7 created Pu=H(cert_u||IDu)cert_u + PDAS  .....");
		byte[] pubKeyBytes = publicKey.getQ().getEncoded(true);

		// Calculates the public key of the client and put it in the hashmap
		// Concatenate client's certificate with its identity
		byte[] certIDconcat = concatByteArrays(encodedCert_u, clientIDbytes);
		// Do sha256 of the concatenation
		multi = System.nanoTime();
		byte[] hash = sha256(certIDconcat);
		System.out.println("******SHA256_5_7: " + (System.nanoTime() - multi));
		// Elliptic curve multiplication with the point certificate
		multi = System.nanoTime();
		ECPoint intermPoint = cert_u.multiply(new BigInteger(hash));
		System.out.println("******star_multi5_7: " + (System.nanoTime() - multi));
		// Point representation of the public key of the client
		multi = System.nanoTime();
		ECPoint pubKeyClientPoint = intermPoint.add(publicKey.getQ());
		System.out.println("******star_add5_7: " + (System.nanoTime() - multi));

		// Public key of the client
		ECPublicKeyParameters pubKeyClient = new ECPublicKeyParameters(pubKeyClientPoint, domainParams);
		clientsPublicKeys.put(clientID, pubKeyClient);

		return toHex(encodedCert_u) + "|" + toHex(qUser.toByteArray()) + "|" + toHex(pubKeyBytes);
	}

//	public static String resourceRegistrationReq(String clientID, String timestamp, String ciphertext, String nonce) {
//		byte[] cleartext = null;
//		byte[] ciphertextBytes = hexStringToByteArray(ciphertext);
//		/* Compute the key Kr = H(k*Pu||Tr) used to decrypt the ciphertext */
//		/* Elliptic curve multiplication */
//		ECPublicKeyParameters pubKeyClient = clientsPublicKeys.get(clientID);
//		ECPoint secretPoint = pubKeyClient.getQ().multiply(privateKey.getD());
//		byte[] encodedSecretPoint = secretPoint.getEncoded(true);
//		// Concatenate encoded secret point with the received timestamp
//		byte[] secretTimestampEncoded = concatByteArrays(encodedSecretPoint, hexStringToByteArray(timestamp));
//		// Do sha256 to obtain the symmetric key
//		byte[] Kr = sha256(secretTimestampEncoded);
//		System.out.println("Symmetric key: " + toHex(Kr));
//		
//		// Decrypt the cipher text to obtain the application specific request of the client
//		CCMBlockCipher ccm = new CCMBlockCipher(new AESEngine());
//		ccm.init(false, new ParametersWithIV(new KeyParameter(Kr), hexStringToByteArray(nonce)));
//		byte[] tmp = new byte[ciphertextBytes.length];
//		int len = ccm.processBytes(ciphertextBytes, 0, ciphertextBytes.length, tmp, 0);
//		try {
//			len += ccm.doFinal(tmp, len);
//			cleartext = new byte[len];
//			System.arraycopy(tmp, 0, cleartext, 0, len);
//			System.out.println("Cleartext: " + toHex(cleartext));
//		} catch (IllegalStateException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvalidCipherTextException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		// Retrieve the application data (resource name and type of subscription)
//		int appDataByteLength = cleartext.length - ServerConstants.randomNumberSize;
//		String appData = toHex(cleartext).substring(0, 2*appDataByteLength);
//		appData = convertHexToString(appData);
//		String[] data = appData.split("\\|\\|");
//		
//		String resName = data[0];
//		String subType = data[1];
//		
//		System.out.println("Resource requested by the client: " + resName);
//		System.out.println("Type of subscription requested by the client: " + subType);
//		
//		// Retrieve the random number c
//		String c = toHex(cleartext).substring(2*appDataByteLength, 2*cleartext.length);
//		System.out.println("Random number c generated by the client: " + c);
//		
//		return resName + "|" + subType + "|" + c;
//	}

	// DAS created Kr
	public static String CreatedKr(String clientID, String timestamp) {
		/* Compute the key Kr = H(k*Pu||Tr) used to decrypt the ciphertext */
		/* Elliptic curve multiplication */
		ECPublicKeyParameters pubKeyClient = clientsPublicKeys.get(clientID);
		long multi = System.nanoTime();
		ECPoint secretPoint = pubKeyClient.getQ().multiply(privateKey.getD());
		System.out.println("******start_multi_6_9: " + (System.nanoTime() - multi));

		byte[] encodedSecretPoint = secretPoint.getEncoded(true);
		// Concatenate encoded secret point with the received timestamp
		byte[] secretTimestampEncoded = concatByteArrays(encodedSecretPoint, hexStringToByteArray(timestamp));
		// Do sha256 to obtain the symmetric key
		multi = System.nanoTime();
		byte[] Kr = sha256(secretTimestampEncoded);
		System.out.println("******SHA256_6_9: " + (System.nanoTime() - multi));
		System.out.println("Symmetric key Kr by the DAS: " + toHex(Kr));
		return toHex(Kr);
	}

	public static String resourceRegistrationReq(String timestamp, String ciphertext, String nonce, String encodeZ) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");
		Date date = new Date();
		System.out.println("***************time start 6.7 BUGGG****************"+dateFormat.format(date));
		time6_7 = System.nanoTime();
		System.out.println("***************Time start 6.7****************"+time6_7);
		System.out.println("\n >>>>>>> Process 6.7 to 6.8 created Kz, D_Kz(Sub) .....");
		byte[] ciphertextBytes = hexStringToByteArray(ciphertext);

		X9ECParameters ecp = SECNamedCurves.getByName("secp256r1");

		/* Compute the key Kz = H(k*Z||Tr) used to decrypt the ciphertext */
		/* Elliptic curve multiplication */

		/* Decode the received encoded Z to obtain the point Z */
		System.out.println("\n >>>>>>> Process 6.7 created Kz = H(k*Z||Tr) .....");
		byte[] Z = hexStringToByteArray(encodeZ);
		ECPoint pointZ = ecp.getCurve().decodePoint(Z);
		long multi = System.nanoTime();
		ECPoint secretPoint = pointZ.multiply(privateKey.getD());
		System.out.println("******star_multi6_7: " + (System.nanoTime() - multi));
		byte[] encodedSecretPoint = secretPoint.getEncoded(true);
		// Concatenate encoded secret point with the received timestamp
		byte[] secretTimestampEncoded = concatByteArrays(encodedSecretPoint, hexStringToByteArray(timestamp));
		// Do sha256 to obtain the symmetric key
		multi = System.nanoTime();
		byte[] Kz = sha256(secretTimestampEncoded);
		System.out.println("******SHA256_6_7: " + (System.nanoTime() - multi));
		System.out.println("Symmetric key Kz: " + toHex(Kz));

		System.out.println("\n >>>>>>> Process 6.8 Decrypt D_Kz(Sub)=> Rn, Type, c, IDu, Kr .....");
		System.out.println("Nonce: " + nonce);

		// Decrypt the cipher text to obtain the application specific request of the
		// client
		multi = System.nanoTime();
		String cleartext = AesGcm256.decrypt(ciphertext, Kz, AesGcm256.HexToByte(nonce));
		System.out.println("******AESGCM_6_8: " + (System.nanoTime() - multi));
		System.out.println("Decrypted Sub: " + cleartext);

		String appData = convertHexToString(cleartext);
		String[] data = appData.split("\\|\\|");

		String resName = data[0];
		String subType = data[1];
		String clientID = data[3];
		String Kr = data[4];
		String c = data[2];

		hexStringToByteArray(c);
		System.out.println("Kr by the client: " + Kr);
		// resName || Subtype|| c || ClientID || Kr

		System.out.println("Resource requested by the client: " + resName);
		System.out.println("Type of subscription requested by the client: " + subType);
		System.out.println("ClientID by the client: " + convertHexToString(toHex(clientID)));
		// Retrieve the random number c
		// String c = toHex(cleartext).substring(2 * appDataByteLength, 2 *
		// cleartext.length);
		System.out.println("Random number c generated by the client: " + c);
		return resName + "|" + subType + "|" + c + "|" + clientID + "|" + Kr;

		// return appData;
	}

	public static String resourceRegistrationResp(String clientID, String tokenID, String resName, String c, String Kr,
			String Texp, String DBpermission) {
		// Convert the tokenID and resource name in its hexadecimal notation using the
		// ascii standard

		System.out.println("\n >>>>>>> Process 6.10 to 6.13 created Qu, Kt, Ticket, ET .....");

//		//Texp = "a";
//		byte[] tokenIDbytes = hexStringToByteArray(toHex(tokenID));
//		byte[] resNamebytes = hexStringToByteArray(toHex(resName));
//		System.out.println("texp: " + Texp);
//		// Concatenate the tokenID with the resource name (e.g. temperature)
//		// TokenID||Rn||Texp
//		String sepSymb = "||";
//		// Add separation symbol to tokenID
//		byte[] tokenIDResConcat = concatByteArrays(tokenIDbytes, hexStringToByteArray(toHex(sepSymb)));
//		// Add type of resource name
//		tokenIDResConcat = concatByteArrays(tokenIDResConcat, resNamebytes);
//		tokenIDResConcat = concatByteArrays(tokenIDbytes, hexStringToByteArray(toHex(sepSymb)));
//		tokenIDResConcat = concatByteArrays(tokenIDResConcat, hexStringToByteArray(toHex(Texp)));
//		System.out.println("Encode " + toHex(tokenIDResConcat));
		/*
		 * Compute the key Ks
		 */
//		X9ECParameters ecp = SECNamedCurves.getByName("secp256r1");
//		ECDomainParameters domainParams = new ECDomainParameters(ecp.getCurve(), ecp.getG(), ecp.getN(), ecp.getH(),
//				ecp.getSeed());
//		/* Generate a random number with a fixed size of 32 bytes */
//		SecureRandom random = new SecureRandom();
//		resRegRandomR = new byte[ServerConstants.randomNumberSize];
//		random.nextBytes(resRegRandomR); // Fill the array with random bytes
//		System.out.println("R = " + toHex(resRegRandomR));
//
//		/* Elliptic curve multiplication using the random number */
//		ECPoint pointR = domainParams.getG().multiply(new BigInteger(resRegRandomR));
//		byte[] SecretpointR = pointR.getEncoded(true);
//		byte[] Ks = sha256(SecretpointR);
		System.out.println("Symmetric key Ks: " + toHex(ServerConstants.Ks));

//		// Generate a random number
//		SecureRandom random = new SecureRandom(); 
//		byte[] r = new byte[ServerConstants.randomNumberSize]; // Create a byte array with a size of 32 bytes
//		random.nextBytes(r); // Fill the array with random bytes

		/* Compute the key Qu = H(d*P_DAS||Tr) */

//		byte[] secretTimestampConcat = concatByteArrays(hexStringToByteArray(toHex(clientID)),
//				hexStringToByteArray(toHex(c)));
//		/* Do the sha256 of the secretTimestampConcat byte array */
//		byte[] Qu = sha256(secretTimestampConcat);
//
//		System.out.println("Qu: " + toHex(Qu));

		byte[] Ks = hexStringToByteArray(ServerConstants.Ks);

		byte[] clientIDBytes = hexStringToByteArray(clientID);
		// Concatenate the identity with the random number generated during resource
		// registration
		System.out.println("\n >>>>>>> Process 6.10 created Qu = H(IDu||c) .....");
		byte[] IDresRegRandomConcat = concatByteArrays(clientIDBytes, hexStringToByteArray(c));
		// Do the sha256 of the concatenation
		long multi = System.nanoTime();
		byte[] Qu = sha256(IDresRegRandomConcat);
		System.out.println("******SHA256_6_10: " + (System.nanoTime() - multi));

		System.out.println("Qu: " + toHex(Qu));

		// Compute the key Kt = H(Qu||Ks)
		System.out.println("\n >>>>>>> Process 6.11 created Kt = H(Qu||Ks) .....");
		byte[] IDprivRandConcat = concatByteArrays(Qu, Ks);
		multi = System.nanoTime();
		byte[] Kt = sha256(IDprivRandConcat);
		System.out.println("******SHA256_6_11: " + (System.nanoTime() - multi));

		System.out.println("Kt :" + toHex(Kt));

		// Compute the Ticket for the client
		// Generate a nonce (12 bytes) to be used for AES_256_CCM_8
		SecureRandom random = new SecureRandom();
		random = new SecureRandom();
		byte[] n1 = new byte[ServerConstants.nonceSize];
		random.nextBytes(n1); // Fill the nonce with random bytes
		System.out.println("nonce1 = " + toHex(n1));

		// Encrypt the Ticket use Kt
		System.out.println("\n >>>>>>> Process 6.12 Encrypt Ticket = E_Kt(TokenID||Rn||Texp) .....");

		byte[] tokenIDbytes = hexStringToByteArray(toHex(tokenID));
		byte[] resNamebytes = hexStringToByteArray(toHex(resName));

		// Concatenate the tokenID with the resource name (e.g. temperature)
		// TokenID||Rn||Texp
		String sepSymb = "||";
		// Add separation symbol to tokenID
		byte[] tokenIDResConcat = concatByteArrays(tokenIDbytes, hexStringToByteArray(toHex(sepSymb)));
		// Add type of resource name
		tokenIDResConcat = concatByteArrays(tokenIDResConcat, resNamebytes);
		// Add Texp
		System.out.println(" DBpermission: " + DBpermission);
		System.out.println(" DBpermission: " + toHex(DBpermission));
		tokenIDResConcat = concatByteArrays(tokenIDResConcat, hexStringToByteArray(toHex(sepSymb)));
		tokenIDResConcat = concatByteArrays(tokenIDResConcat, hexStringToByteArray(toHex(Texp)));
		tokenIDResConcat = concatByteArrays(tokenIDResConcat, hexStringToByteArray(toHex(sepSymb)));
		tokenIDResConcat = concatByteArrays(tokenIDResConcat, hexStringToByteArray(toHex(DBpermission)));

		// Encrypt ticket = E_Kt(TokenID||Rn||Texp)
		multi = System.nanoTime();
		String ticket = AesGcm256.encrypt(toHex(tokenIDResConcat), Kt, n1);
		System.out.println("******AESGCM_6_12: " + (System.nanoTime() - multi));
		System.out.println("Encrypted Ticket: " + ticket);

		random = new SecureRandom();
		byte[] n2 = new byte[ServerConstants.nonceSize];
		random.nextBytes(n2); // Fill the nonce with random bytes
		System.out.println("nonce2 = " + toHex(n2));
		System.out.println("Texp = " + Texp);

		String Ticket = ticket;
		System.out.println("Ticket: " + Ticket);
		// Tai sao phai doi sang String: Vi tach ban tin se dung split(chi dung tren
		// String)
		// nen Ticket phai dua ve String sau do dua ve ByteArray
		System.out.println("\n >>>>>>> Process 6.13 created ET = E_Kr(Ticket||Texp) .....");

		// Encrypt ET = E_Kr(Ticket||Texp)
		byte[] ticketTexp = concatByteArrays(hexStringToByteArray(toHex(Ticket)), hexStringToByteArray(toHex(sepSymb)));
		ticketTexp = concatByteArrays(ticketTexp, hexStringToByteArray(toHex(Texp)));
		System.out.println("ticketTexp: " + toHex(ticketTexp));
		multi = System.nanoTime();
		String ET = AesGcm256.encrypt(toHex(ticketTexp), AesGcm256.HexToByte(Kr), n2);
		System.out.println("******AESGCM_6_13: " + (System.nanoTime() - multi));
		System.out.println("Encrypted ET: " + ET);
		System.out.println("********************Time end 6.13***************"+System.nanoTime());
		DateFormat dateFormat11 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");
		Date date11 = new Date();
		System.out.println("***************time end 6.13 BUGGG****************"+dateFormat11.format(date11));
		System.out.println("********************Time process 6.7 to 6.13***************"+(System.nanoTime()-time6_7));
		
//		CCMBlockCipher ccm1 = new CCMBlockCipher(new AESEngine());
//		ccm1.init(true, new ParametersWithIV(new KeyParameter(hexStringToByteArray(Kr)), n2));
//		byte[] ET = new byte[ticketTexp.length + 8];
//		int len2 = ccm1.processBytes(ticketTexp, 0, ticketTexp.length, ET, 0);
//		try {
//			len2 += ccm1.doFinal(ET, len2);
//		} catch (IllegalStateException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvalidCipherTextException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		System.out.println("ET: " + toHex(ET));

		return ET + "|" + toHex(Kt) + "|" + toHex(n1) + "|" + toHex(n2);
	}

	public static String createSessionKey(String clientID, String timestamp) {
		// Compute the symmetric session key SKsession = H(k*Pu||Ts)
		// Elliptic curve multiplication
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");
		Date date = new Date();
		System.out.println("***************time start 7.6 BUGGG****************"+dateFormat.format(date));
		long time7_6 = System.nanoTime();
		System.out.println("************ Time start 7.6*************** " + (time7_6));
		System.out.println("\n >>>>>>> Process 7.6 created SK = H(k*Pu||Ts) .....");
		// System.out.println("timeStamp DAS: "+timestamp);
		ECPublicKeyParameters pubKeyClient = clientsPublicKeys.get(clientID);
		long multi = System.nanoTime();
		ECPoint secretPoint = pubKeyClient.getQ().multiply(privateKey.getD());
		System.out.println("******star_multi7_6: " + (System.nanoTime() - multi));
		byte[] encodedSecretPoint = secretPoint.getEncoded(true);
		// Concatenate encoded secret point with the received timestamp
		byte[] secretTimestampEncoded = concatByteArrays(encodedSecretPoint, hexStringToByteArray(timestamp));
		// Do sha256 to obtain the symmetric key
		multi = System.nanoTime();
		byte[] SKsession = sha256(secretTimestampEncoded);
		System.out.println("******SHA256_7_6: " + (System.nanoTime() - multi));
		System.out.println("Symmetric session key: " + toHex(SKsession));
		System.out.println("************ Time end 7.6*************** " + (System.nanoTime()));
		DateFormat dateFormat2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");
		Date date2 = new Date();
		System.out.println("***************time end 7.6 BUGGG****************"+dateFormat.format(date2));
		System.out.println("************ Time process 7.6*******************: " + (System.nanoTime() - time7_6));
		
		return toHex(SKsession);
	}

	public static ECPrivateKeyParameters getPrivateKey() {
		return privateKey;
	}

	public static ECPublicKeyParameters getPublicKey() {
		return publicKey;
	}

	public static Map<String, String> getClientIDandq() {
		return clientsIDandq;
	}
}
