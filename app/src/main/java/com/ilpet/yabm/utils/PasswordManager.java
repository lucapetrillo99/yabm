package com.ilpet.yabm.utils;

import android.util.Base64;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class PasswordManager {
    private static final String INIT_VECTOR = "encryptionIntVec";
    private static final String SECRET_KEY = "aesEncryptionKey";
    private static PasswordManager instance = null;

    private PasswordManager() {
    }

    public static PasswordManager getInstance() {
        if (instance == null)
            instance = new PasswordManager();

        return instance;
    }

    public String encryptPassword(String value) {
        try {
            IvParameterSpec ivParam = new IvParameterSpec(INIT_VECTOR.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParam);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception ex) {
            return null;
        }
    }

    public String decryptPassword(String value) {
        try {
            IvParameterSpec ivParam = new IvParameterSpec(INIT_VECTOR.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParam);
            byte[] original = cipher.doFinal(Base64.decode(value, Base64.DEFAULT));
            return new String(original);
        } catch (Exception ex) {
            return null;
        }
    }
}
