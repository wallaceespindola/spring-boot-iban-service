package com.example.iban.util;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class IbanValidator {

    private IbanValidator() {}

    private static final Map<String, Integer> COUNTRY_IBAN_LENGTH;
    static {
        Map<String, Integer> m = new HashMap<>();
        m.put("AL", 28); m.put("AD", 24); m.put("AT", 20); m.put("AZ", 28);
        m.put("BA", 20); m.put("BE", 16); m.put("BG", 22); m.put("CH", 21);
        m.put("CY", 28); m.put("CZ", 24); m.put("DE", 22); m.put("DK", 18);
        m.put("EE", 20); m.put("ES", 24); m.put("FI", 18); m.put("FO", 18);
        m.put("FR", 27); m.put("GB", 22); m.put("GI", 23); m.put("GL", 18);
        m.put("GR", 27); m.put("HR", 21); m.put("HU", 28); m.put("IE", 22);
        m.put("IS", 26); m.put("IT", 27); m.put("LI", 21); m.put("LT", 20);
        m.put("LU", 20); m.put("LV", 21); m.put("MC", 27); m.put("MD", 24);
        m.put("ME", 22); m.put("MK", 19); m.put("MT", 31); m.put("NL", 18);
        m.put("NO", 15); m.put("PL", 28); m.put("PT", 25); m.put("RO", 24);
        m.put("RS", 22); m.put("SA", 24); m.put("SE", 24); m.put("SI", 19);
        m.put("SK", 24); m.put("SM", 27); m.put("TR", 26); m.put("UA", 29);
        m.put("XK", 20); m.put("AE", 23); m.put("BH", 22); m.put("CR", 22);
        m.put("DO", 28); m.put("EG", 29); m.put("GE", 22); m.put("GT", 28);
        m.put("IQ", 23); m.put("JO", 30); m.put("KW", 30); m.put("KZ", 20);
        m.put("LB", 28); m.put("LC", 32); m.put("MR", 27); m.put("MU", 30);
        m.put("PK", 24); m.put("PS", 29); m.put("QA", 29); m.put("SC", 31);
        m.put("SV", 28); m.put("TL", 23); m.put("TN", 24); m.put("VG", 24);
        m.put("BY", 28); m.put("VA", 22);
        COUNTRY_IBAN_LENGTH = Collections.unmodifiableMap(m);
    }

    public static Map<String, Integer> countryLengthMap() {
        return COUNTRY_IBAN_LENGTH;
    }

    public static ValidationResult validate(String iban) {
        if (iban == null) return ValidationResult.invalid("null input");
        String raw = iban.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        if (raw.length() < 4 || !raw.matches("[A-Z0-9]+")) {
            return ValidationResult.invalid("invalid characters or length < 4");
        }
        String cc = raw.substring(0, 2);
        Integer expected = COUNTRY_IBAN_LENGTH.get(cc);
        if (expected == null) {
            return ValidationResult.invalid("unsupported country: " + cc);
        }
        if (raw.length() != expected) {
            return ValidationResult.invalid("length mismatch for " + cc + ": expected " + expected + ", got " + raw.length());
        }
        String rearranged = raw.substring(4) + raw.substring(0, 4);
        int remainder = mod97(rearranged);
        boolean ok = remainder == 1;
        return ok ? ValidationResult.valid(raw) : ValidationResult.invalid("mod97 != 1 (" + remainder + ")");
    }

    // Progressive mod97 over alphanum string where A=10..Z=35 (no large integer parsing)
    public static int mod97(String s) {
        int rem = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isDigit(c)) {
                rem = (rem * 10 + (c - '0')) % 97;
            } else {
                int val = c - 'A' + 10; // 10..35
                // process the two decimal digits of val
                rem = (rem * 10 + (val / 10)) % 97;
                rem = (rem * 10 + (val % 10)) % 97;
            }
        }
        return rem;
    }

    public record ValidationResult(boolean valid, String iban, String message, String timestamp) {
        public static ValidationResult valid(String normalized) {
            return new ValidationResult(true, normalized, "OK", Instant.now().toString());
        }
        public static ValidationResult invalid(String msg) {
            return new ValidationResult(false, null, msg, Instant.now().toString());
        }
    }
}
