package com.expensetracker.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordUtils Tests")
class PasswordUtilsTest {

    @Test
    @DisplayName("hashPassword returns non-null non-empty result")
    void testHashReturnsNonNull() {
        String hash = PasswordUtils.hashPassword("myPassword123");
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
    }

    @Test
    @DisplayName("hashPassword is deterministic - same input gives same hash")
    void testHashDeterministic() {
        String hash1 = PasswordUtils.hashPassword("testPassword");
        String hash2 = PasswordUtils.hashPassword("testPassword");
        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Different passwords produce different hashes")
    void testDifferentPasswordsDifferentHashes() {
        String hash1 = PasswordUtils.hashPassword("password1");
        String hash2 = PasswordUtils.hashPassword("password2");
        assertNotEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Hash is Base64 encoded and reasonable length")
    void testHashIsBase64() {
        String hash = PasswordUtils.hashPassword("test");
        // SHA-256 = 32 bytes, Base64 of 32 bytes = 44 chars (with padding)
        assertEquals(44, hash.length());
        // Verify it's valid Base64 - decoding should not throw
        assertDoesNotThrow(() -> java.util.Base64.getDecoder().decode(hash));
    }

    @Test
    @DisplayName("Empty password still produces a hash")
    void testEmptyPassword() {
        String hash = PasswordUtils.hashPassword("");
        assertNotNull(hash);
        assertEquals(44, hash.length());
    }

    @Test
    @DisplayName("Case-sensitive passwords produce different hashes")
    void testCaseSensitive() {
        String hash1 = PasswordUtils.hashPassword("Password");
        String hash2 = PasswordUtils.hashPassword("password");
        assertNotEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Special characters in password are handled")
    void testSpecialCharacters() {
        String hash = PasswordUtils.hashPassword("p@$$w0rd!#%^&*");
        assertNotNull(hash);
        assertEquals(44, hash.length());
    }

    @Test
    @DisplayName("Long password is handled")
    void testLongPassword() {
        String longPassword = "a".repeat(10000);
        String hash = PasswordUtils.hashPassword(longPassword);
        assertNotNull(hash);
        assertEquals(44, hash.length());
    }
}
