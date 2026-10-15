package com.petadoption.util;

import com.petadoption.exception.ValidationException;

import java.util.regex.Pattern;

/** Small collection of input checks used by the service layer. */
public final class Validator {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9 -]{8,15}$");

    private Validator() {
    }

    public static String requireText(String field, String value, int min, int max) throws ValidationException {
        String text = value == null ? "" : value.trim();
        if (text.length() < min) {
            throw new ValidationException(field + " must be at least " + min + " character" + (min == 1 ? "" : "s") + " long.");
        }
        if (text.length() > max) {
            throw new ValidationException(field + " must be at most " + max + " characters long.");
        }
        return text;
    }

    public static String optionalText(String field, String value, int max) throws ValidationException {
        String text = value == null ? "" : value.trim();
        if (text.length() > max) {
            throw new ValidationException(field + " must be at most " + max + " characters long.");
        }
        return text;
    }

    public static String requireEmail(String value) throws ValidationException {
        String email = requireText("Email", value, 5, 150).toLowerCase();
        if (!EMAIL.matcher(email).matches()) {
            throw new ValidationException("Please enter a valid email address.");
        }
        return email;
    }

    public static void requirePassword(String value) throws ValidationException {
        if (value == null || value.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long.");
        }
    }

    public static String requirePhone(String field, String value) throws ValidationException {
        String phone = requireText(field, value, 8, 15);
        if (!PHONE.matcher(phone).matches()) {
            throw new ValidationException(field + " must contain 8-15 digits.");
        }
        return phone;
    }

    public static int requireInt(String field, String value, int min, int max) throws ValidationException {
        try {
            int n = Integer.parseInt(value == null ? "" : value.trim());
            if (n < min || n > max) {
                throw new ValidationException(field + " must be between " + min + " and " + max + ".");
            }
            return n;
        } catch (NumberFormatException e) {
            throw new ValidationException(field + " must be a whole number.");
        }
    }
}
