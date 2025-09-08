package com.example.iban.service;

import com.example.iban.util.IbanValidator;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;

public class BelgiumIbanGenerator {
    private static final SecureRandom RNG = new SecureRandom();

    /**
     * Belgium IBAN structure: BEkk bbbb bbbb bbbc
     * BBAN = 12 digits: 3 bank + 7 account + 2 checksum (mod 97, 97 -> 97)
     */
    public static GeneratedIban generate() {
        int bank = 100 + RNG.nextInt(900); // 3 digits, not starting with 0
        int account7 = RNG.nextInt(10_000_000); // 0..9,999,999
        String base10 = String.format(Locale.ROOT, "%03d%07d", bank, account7);
        // Compute mod 97 safely over the 10-digit string without parsing to int
        int mod = 0;
        for (int i = 0; i < base10.length(); i++) {
            mod = (mod * 10 + (base10.charAt(i) - '0')) % 97;
        }
        int bbanChecksum = 97 - mod;
        if (bbanChecksum == 0) bbanChecksum = 97;
        String bban = base10 + String.format(Locale.ROOT, "%02d", bbanChecksum);
        String country = "BE";
        String with00 = country + "00" + bban;
        String rearranged = with00.substring(4) + with00.substring(0, 4);
        int mod2 = IbanValidator.mod97(rearranged);
        int check = 98 - mod2;
        String checkStr = String.format(Locale.ROOT, "%02d", check);
        String iban = country + checkStr + bban;
        return new GeneratedIban(iban, Instant.now().toString());
    }

    public record GeneratedIban(String iban, String timestamp) {}
}
