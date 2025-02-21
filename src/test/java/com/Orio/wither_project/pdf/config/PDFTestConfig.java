package com.Orio.wither_project.pdf.config;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@Profile("test")
public class PDFTestConfig {

    @Bean
    PDDocument testPDDocument() throws IOException {
        try (InputStream pdfInputStream = getClass().getClassLoader().getResourceAsStream("test.pdf")) { // TODO extract
                                                                                                         // to constants
                                                                                                         // (resources)
            if (pdfInputStream != null) {
                return Loader.loadPDF(pdfInputStream.readAllBytes());
            }
            throw new IOException("Test PDF not found");
        }
    }
}
