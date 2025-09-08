package com.example.iban;

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
}
