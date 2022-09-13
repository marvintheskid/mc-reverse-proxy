package me.marvin.proxy.utils;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;

/**
 * Cipher-related utils.
 */
public interface MinecraftEncryption {
    /**
     * Generates the required secret key for packet encryption.
     *
     * @return the keypair
     */
    static SecretKey generateSecretKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Hashes the given server id using the public and the secret key.
     *
     * @param serverId  the server id
     * @param publicKey the public key
     * @param secretKey the shared secret key
     * @return the hashed {@code serverId}
     */
    static byte[] hashServerId(String serverId, PublicKey publicKey, SecretKey secretKey) {
        try {
            return operateDigest("SHA-1", serverId.getBytes("ISO_8859_1"), secretKey.getEncoded(), publicKey.getEncoded());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Creates (reconstitutes) a public RSA key using the given encoded key.
     *
     * @param encodedKey the encoded key
     * @return a new RSA public key
     */
    static PublicKey createRSAPublicKey(byte[] encodedKey) {
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
            KeyFactory rsaFactory = KeyFactory.getInstance("RSA");
            return rsaFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Decrypts the given data using the given key.
     *
     * @param key  the key
     * @param data the data
     * @return the decrypted data
     */
    static byte[] decryptData(Key key, byte[] data) { // Feather: renamed method: b(Key, byte[]) -> decryptData(Key, byte[])
        return createAndOperate(Cipher.DECRYPT_MODE, key, data);
    }

    /**
     * Encrypts the given data using the given key.
     *
     * @param key  the key
     * @param data the data
     * @return the encrypted data
     */
    static byte[] encryptData(Key key, byte[] data) { // Feather: renamed method: b(Key, byte[]) -> decryptData(Key, byte[])
        return createAndOperate(Cipher.ENCRYPT_MODE, key, data);
    }

    /**
     * Creates an AES/CFB8/NoPadding cipher used for packet encryption / decryption.
     *
     * @param mode the mode
     * @param key  the key
     * @return the cipher
     */
    static Cipher createEncryptionCipher(int mode, Key key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
            cipher.init(mode, key, new IvParameterSpec(key.getEncoded()));
            return cipher;
        } catch (GeneralSecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Computes a message digest based on the given data with the given algorithm.
     *
     * @param algorithm the algorithm
     * @param data      the data
     * @return the digest
     */
    private static byte[] operateDigest(String algorithm, byte[]... data) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);

            for (byte[] dataBlock : data) {
                digest.update(dataBlock);
            }

            return digest.digest();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Creates a new instance of the given key's cipher, then finishes the operation with the given data.
     *
     * @param mode the mode of the operation
     * @param key  the key
     * @param data the data
     * @return the result
     */
    private static byte[] createAndOperate(int mode, Key key, byte[] data) {
        try {
            return createCipher(key.getAlgorithm(), mode, key).doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Creates a cipher of the given type.
     *
     * @param cipherType the type of the cipher
     * @param mode       the mode
     * @param key        the key
     * @return the cipher
     */
    private static Cipher createCipher(String cipherType, int mode, Key key) {
        try {
            Cipher cipher = Cipher.getInstance(cipherType);
            cipher.init(mode, key);
            return cipher;
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
