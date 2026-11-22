package com.petadoption.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.petadoption.exception.ValidationException;
import org.junit.Test;

/** Unit tests for {@link Validator} (pure logic, no database needed). */
public class ValidatorTest {

    /** Asserts that the given call throws a ValidationException whose message contains the text. */
    private interface Call {
        void run() throws Exception;
    }

    private static void assertInvalid(Call call, String messagePart) {
        try {
            call.run();
            fail("Expected ValidationException containing: " + messagePart);
        } catch (ValidationException e) {
            assertTrue("Unexpected message: " + e.getMessage(), e.getMessage().contains(messagePart));
        } catch (Exception e) {
            fail("Wrong exception type: " + e);
        }
    }

    // ---- requireText / optionalText ----

    @Test
    public void requireText_trimsAndReturnsValue() throws Exception {
        assertEquals("Bruno", Validator.requireText("Name", "  Bruno  ", 1, 10));
    }

    @Test
    public void requireText_tooShort_throws() {
        assertInvalid(() -> Validator.requireText("Name", "a", 2, 10), "at least 2 characters");
    }

    @Test
    public void requireText_null_throws() {
        assertInvalid(() -> Validator.requireText("Name", null, 1, 10), "at least 1 character");
    }

    @Test
    public void requireText_tooLong_throws() {
        assertInvalid(() -> Validator.requireText("Name", "abcdefghijk", 1, 10), "at most 10");
    }

    @Test
    public void optionalText_nullBecomesEmpty() throws Exception {
        assertEquals("", Validator.optionalText("Notes", null, 50));
    }

    @Test
    public void optionalText_tooLong_throws() {
        assertInvalid(() -> Validator.optionalText("Notes", "abcdef", 5), "at most 5");
    }

    // ---- requireEmail ----

    @Test
    public void requireEmail_validEmail_isLowerCasedAndTrimmed() throws Exception {
        assertEquals("test@example.com", Validator.requireEmail("  Test@Example.COM "));
    }

    @Test
    public void requireEmail_missingAtSign_throws() {
        assertInvalid(() -> Validator.requireEmail("not-an-email"), "valid email");
    }

    @Test
    public void requireEmail_tooShort_throws() {
        assertInvalid(() -> Validator.requireEmail("a@b"), "at least 5");
    }

    // ---- requirePassword ----

    @Test
    public void requirePassword_sixCharacters_isAccepted() throws Exception {
        Validator.requirePassword("abc123");
    }

    @Test
    public void requirePassword_fiveCharacters_throws() {
        assertInvalid(() -> Validator.requirePassword("abc12"), "at least 6");
    }

    @Test
    public void requirePassword_null_throws() {
        assertInvalid(() -> Validator.requirePassword(null), "at least 6");
    }

    // ---- requirePhone ----

    @Test
    public void requirePhone_validNumber_isAccepted() throws Exception {
        assertEquals("9876543210", Validator.requirePhone("Phone", "9876543210"));
    }

    @Test
    public void requirePhone_tooShort_throws() {
        assertInvalid(() -> Validator.requirePhone("Phone", "12345"), "at least 8");
    }

    @Test
    public void requirePhone_containsLetters_throws() {
        assertInvalid(() -> Validator.requirePhone("Phone", "98765abc10"), "8-15 digits");
    }

    // ---- requireInt ----

    @Test
    public void requireInt_inRange_returnsNumber() throws Exception {
        assertEquals(5, Validator.requireInt("Age", " 5 ", 0, 30));
    }

    @Test
    public void requireInt_outOfRange_throws() {
        assertInvalid(() -> Validator.requireInt("Age", "31", 0, 30), "between 0 and 30");
    }

    @Test
    public void requireInt_notANumber_throws() {
        assertInvalid(() -> Validator.requireInt("Age", "abc", 0, 30), "whole number");
    }

    @Test
    public void requireInt_null_throws() {
        assertInvalid(() -> Validator.requireInt("Age", null, 0, 30), "whole number");
    }
}
