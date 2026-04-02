package com.stockmanager.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void hashPassword_producesNonNullHash() {
        String hash = PasswordUtil.hashPassword("secret");
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
    }

    @Test
    void hashPassword_producesDifferentHashesForSameInput() {
        // BCrypt uses a random salt so two hashes of the same password differ
        String h1 = PasswordUtil.hashPassword("password");
        String h2 = PasswordUtil.hashPassword("password");
        assertNotEquals(h1, h2);
    }

    @Test
    void checkPassword_returnsTrueForCorrectPassword() {
        String hash = PasswordUtil.hashPassword("admin123");
        assertTrue(PasswordUtil.checkPassword("admin123", hash));
    }

    @Test
    void checkPassword_returnsFalseForWrongPassword() {
        String hash = PasswordUtil.hashPassword("admin123");
        assertFalse(PasswordUtil.checkPassword("wrongpass", hash));
    }

    @Test
    void checkPassword_returnsFalseWhenPasswordIsNull() {
        String hash = PasswordUtil.hashPassword("admin123");
        assertFalse(PasswordUtil.checkPassword(null, hash));
    }

    @Test
    void checkPassword_returnsFalseWhenHashIsNull() {
        assertFalse(PasswordUtil.checkPassword("admin123", null));
    }
}
