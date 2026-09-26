package com.example.iban;

import com.example.iban.service.BelgiumIbanGenerator;
import com.example.iban.service.IbanService;
import com.example.iban.util.IbanValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IbanValidatorTests {

    @Test
    void validExamplesPass() {
        assertTrue(IbanValidator.validate("BE71 0961 2345 6769").valid());
        assertTrue(IbanValidator.validate("DE89 3704 0044 0532 0130 00").valid());
        assertTrue(IbanValidator.validate("FR14 2004 1010 0505 0001 3M02 606").valid());
        assertTrue(IbanValidator.validate("NL91 ABNA 0417 1643 00").valid());
    }

    @Test
    void wrongChecksumFails() {
        assertFalse(IbanValidator.validate("BE71 0961 2345 6768").valid());
    }

    @Test
    void unknownCountryFails() {
        assertFalse(IbanValidator.validate("ZZ0012345678901234").valid());
    }

    @Test
    void generatedBelgianIbansPassNationalChecksum() {
        for (int i = 0; i < 1_000; i++) {
            BelgiumIbanGenerator.GeneratedIban be = BelgiumIbanGenerator.generate();
            assertTrue(IbanValidator.validate(be.iban()).valid(), be.iban());
            assertEquals(be.iban().substring(4), be.bban());
            assertBelgianChecksum(be.bban());
        }
        assertBelgianChecksum(IbanService.generateForCountry("be").bban());
    }

    private static void assertBelgianChecksum(String bban) {
        long mod = Long.parseLong(bban.substring(0, 10)) % 97;
        assertEquals(mod == 0 ? 97 : mod, Long.parseLong(bban.substring(10)), bban);
    }
}
