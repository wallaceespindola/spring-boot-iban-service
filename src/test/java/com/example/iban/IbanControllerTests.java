package com.example.iban;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class IbanControllerTests {

    @Autowired MockMvc mvc;

    @Test
    void generateBEWorks() throws Exception {
        mvc.perform(get("/api/iban/be/generate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.country").value("BE"))
                .andExpect(jsonPath("$.iban").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void generateForCountryAddsTimestamp() throws Exception {
        mvc.perform(get("/api/iban/BE/generate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iban").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void validateEndpointResponds() throws Exception {
        mvc.perform(get("/api/iban/validate").param("iban", "BE71096123456769"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
