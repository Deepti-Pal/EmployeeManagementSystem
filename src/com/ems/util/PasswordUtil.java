package com.ems.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * PasswordUtil - SHA-256 hashing for secure password storage.
 */
public class PasswordUtil {

    private PasswordUtil() {}

    /**
     * Hashes a plain-text password using SHA-256.
     * @param plainText the raw password
     * @return 64-character hex digest
     */
    public static String hashPassword(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plainText.getBytes());
            BigInteger number = new BigInteger(1, hash);
            StringBuilder sb = new StringBuilder(number.toString(16));
            while (sb.length() < 64) sb.insert(0, '0');
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available.", e);
        }
    }

    /**
     * Verifies a plain-text password against its stored hash.
     */
    public static boolean verifyPassword(String plainText, String storedHash) {
        return hashPassword(plainText).equalsIgnoreCase(storedHash);
    }
}
