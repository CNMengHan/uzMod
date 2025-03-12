package com.uuz.fabrictestproj.utils;

import java.util.Base64;

public class SecurityUtils {
    /**
     * @param encryptedStr en
     * @return de
     */
    public static String decryptTripleBase64(String encryptedStr) {
        String result = encryptedStr;
        for (int i = 0; i < 3; i++) {
            result = new String(Base64.getDecoder().decode(result));
        }
        return result;
    }
} 