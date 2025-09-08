package com.example.iban;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(
        title = "IBAN Generator & Validator API",
        version = "v1",
        description = "Endpoints to generate and validate IBANs, including a Belgium-specific generator."
))
@SpringBootApplication
public class IbanApplication {
    public static void main(String[] args) {
        SpringApplication.run(IbanApplication.class, args);
    }
}
