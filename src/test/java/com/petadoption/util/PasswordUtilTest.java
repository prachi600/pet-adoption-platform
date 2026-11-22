package com.petadoption.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Unit tests for {@link PasswordUtil} (salted PBKDF2 hashing). */
public class PasswordUtilTest {

    @Test
    public void newSalt_isNotEmptyAndDifferentEachTime() {
        String first = PasswordUtil.newSalt();
        String second = PasswordUtil.newSalt();
        assertNotNull(first);
        assertFalse(first.isEmpty());
        assertNotEquals(first, second);
    }

    @Test
    public void hash_isNeverThePlainPassword() {
        String salt = PasswordUtil.newSalt();
        assertNotEquals("Admin@123", PasswordUtil.hash("Admin@123", salt));
    }

    @Test
    public void hash_sameInputGivesSameHash() {
        String salt = PasswordUtil.newSalt();
        assertEquals(PasswordUtil.hash("Secret#1", salt), PasswordUtil.hash("Secret#1", salt));
    }

    @Test
    public void hash_differentSaltGivesDifferentHash() {
        String hash1 = PasswordUtil.hash("Secret#1", PasswordUtil.newSalt());
        String hash2 = PasswordUtil.hash("Secret#1", PasswordUtil.newSalt());
        assertNotEquals(hash1, hash2);
    }

    @Test
    public void matches_correctPassword_returnsTrue() {
        String salt = PasswordUtil.newSalt();
        String hash = PasswordUtil.hash("Adopter@123", salt);
        assertTrue(PasswordUtil.matches("Adopter@123", salt, hash));
    }

    @Test
    public void matches_wrongPassword_returnsFalse() {
        String salt = PasswordUtil.newSalt();
        String hash = PasswordUtil.hash("Adopter@123", salt);
        assertFalse(PasswordUtil.matches("adopter@123", salt, hash));
    }
}
