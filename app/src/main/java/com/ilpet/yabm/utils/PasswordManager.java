package com.ilpet.yabm.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordManager {
    private static final int BCRYPT_WORK_FACTOR = 12;
    private static PasswordManager instance = null;

    private PasswordManager() {
    }

    public static PasswordManager getInstance() {
        if (instance == null)
            instance = new PasswordManager();

        return instance;
    }

    public String hashPassword(String password) {
        String salt = BCrypt.gensalt(BCRYPT_WORK_FACTOR);
        return BCrypt.hashpw(password, salt);
    }

    public boolean verifyPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
}
