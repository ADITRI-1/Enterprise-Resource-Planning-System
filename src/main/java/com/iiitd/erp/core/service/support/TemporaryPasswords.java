package com.iiitd.erp.core.service.support;

import java.security.SecureRandom;

/** Random one-time passwords handed out when an admin creates or resets an account. */
public final class TemporaryPasswords {

    // Ambiguous characters (0/O, 1/l/I) are left out so passwords can be read aloud or copied by hand.
    private static final String LETTERS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz";
    private static final String DIGITS = "23456789";
    private static final String ALL = LETTERS + DIGITS;
    private static final int LENGTH = 12;
    private static final SecureRandom RANDOM = new SecureRandom();

    private TemporaryPasswords() {}

    public static String generate() {
        char[] chars = new char[LENGTH];
        chars[0] = LETTERS.charAt(RANDOM.nextInt(LETTERS.length()));
        chars[1] = DIGITS.charAt(RANDOM.nextInt(DIGITS.length()));
        for (int i = 2; i < LENGTH; i++) {
            chars[i] = ALL.charAt(RANDOM.nextInt(ALL.length()));
        }
        for (int i = LENGTH - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        return new String(chars);
    }
}
