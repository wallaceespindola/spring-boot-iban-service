package com.example.iban.controller;

import com.example.iban.service.BelgiumIbanGenerator;
import com.example.iban.service.IbanService;
import com.example.iban.util.IbanValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/iban", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "IBAN", description = "Generate and validate IBANs")
public class IbanController {

    @Operation(
            summary = "Validate an IBAN",
            description = "Runs length, character and Mod-97 checks. Returns a timestamp with the result.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Validation result",
                            content = @Content(schema = @Schema(implementation = IbanValidator.ValidationResult.class)))
            }
    )
    @GetMapping("/validate")
    public IbanValidator.ValidationResult validate(
            @Parameter(description = "IBAN to validate", example = "BE71096123456769")
            @RequestParam String iban) {
        return IbanService.validate(iban);
    }

    @Operation(
            summary = "Generate a valid IBAN for a given country",
            description = "Generates a structurally valid IBAN (correct length and check digits). BBAN is randomized.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Generated IBAN with timestamp")
            }
    )
    @GetMapping("/{country}/generate")
    public Map<String, Object> generate(
            @Parameter(description = "2-letter country code (ISO 3166-1 alpha-2)", example = "BE")
            @PathVariable String country) {
        IbanService.GeneratedIban gi = IbanService.generateForCountry(country);
        return Map.of(
                "country", country.toUpperCase(),
                "iban", gi.iban(),
                "message", gi.message(),
                "timestamp", gi.timestamp()
        );
    }

    @Operation(
            summary = "Generate a Belgium IBAN",
            description = "Belgium-specific generator that also enforces the local BBAN checksum before computing IBAN check digits.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Generated Belgium IBAN with timestamp")
            }
    )
    @GetMapping("/be/generate")
    public Map<String, Object> generateBelgium() {
        BelgiumIbanGenerator.GeneratedIban gi = BelgiumIbanGenerator.generate();
        return Map.of(
                "country", "BE",
                "iban", gi.iban(),
                "timestamp", gi.timestamp()
        );
    }

    @Operation(summary = "List supported countries")
    @GetMapping("/countries")
    public Map<String, Object> countries() {
        return Map.of(
                "supported", IbanValidator.countryLengthMap().keySet(),
                "timestamp", Instant.now().toString()
        );
    }
}
