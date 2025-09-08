package com.example.iban.service;

import com.example.iban.util.IbanValidator;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;

public class IbanService {

    private static final SecureRandom RNG = new SecureRandom();

    public static GeneratedIban generateForCountry(String countryCode) {
        String cc = countryCode == null ? "" : countryCode.trim().toUpperCase(Locale.ROOT);
        Map<String, Integer> map = IbanValidator.countryLengthMap();
        Integer len = map.get(cc);
        if (len == null) {
            return new GeneratedIban(null, "unsupported country: " + cc, Instant.now().toString());
        }
        int bbanLen = len - 4;
        String bban = randomDigits(bbanLen);
        String temp = cc + "00" + bban;
        String rearranged = temp.substring(4) + temp.substring(0, 4);
        int mod = IbanValidator.mod97(rearranged);
        int check = 98 - mod;
        String checkStr = String.format(Locale.ROOT, "%02d", check);
        String iban = cc + checkStr + bban;
        return new GeneratedIban(iban, "OK", Instant.now().toString());
    }

    public static IbanValidator.ValidationResult validate(String iban) {
        return IbanValidator.validate(iban);
    }

    private static String randomDigits(int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(RNG.nextInt(10));
        return sb.toString();
    }

    public record GeneratedIban(String iban, String message, String timestamp) {}
}
