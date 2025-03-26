package com.Orio.wither_project.core.controller;

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@Validated
@RequiredArgsConstructor
public class WitherHomeController {
    private static final Logger logger = getLogger(WitherHomeController.class);

    @Operation(summary = "Home endpoint")
    @ApiResponse(responseCode = "200", description = "Welcome message")
    @GetMapping(value = { "", "/" })
    public ResponseEntity<String> home() {
        logger.debug("Home endpoint accessed");
        return ResponseEntity.ok("Welcome to the Wither Project!");
    }
}