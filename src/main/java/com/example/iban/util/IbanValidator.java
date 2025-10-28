package com.example.iban.util;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class IbanValidator {

    private IbanValidator() {}

    private enum Country {
        AL(28), AD(24), AT(20), AZ(28),
        BA(20), BE(16), BG(22), CH(21),
        CY(28), CZ(24), DE(22), DK(18),
        EE(20), ES(24), FI(18), FO(18),
        FR(27), GB(22), GI(23), GL(18),
        GR(27), HR(21), HU(28), IE(22),
        IS(26), IT(27), LI(21), LT(20),
        LU(20), LV(21), MC(27), MD(24),
        ME(22), MK(19), MT(31), NL(18),
        NO(15), PL(28), PT(25), RO(24),
        RS(22), SA(24), SE(24), SI(19),
        SK(24), SM(27), TR(26), UA(29),
        XK(20), AE(23), BH(22), CR(22),
        DO(28), EG(29), GE(22), GT(28),
        IQ(23), JO(30), KW(30), KZ(20),
        LB(28), LC(32), MR(27), MU(30),
        PK(24), PS(29), QA(29), SC(31),
        SV(28), TL(23), TN(24), VG(24),
        BY(28), VA(22);

        private final int length;

        Country(int length) { this.length = length; }

        public int length() { return length; }
    }

    private static final Map<String, Integer> COUNTRY_IBAN_LENGTH;
    static {
        Map<String, Integer> m = new HashMap<>();
        for (Country c : Country.values()) {
            m.put(c.name(), c.length());
        }
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
