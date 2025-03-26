package com.Orio.wither_project.pdf.summary.service.conversion.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.Orio.wither_project.pdf.model.entity.FileEntity;
import com.Orio.wither_project.process.summary.model.DocumentModel;
import com.Orio.wither_project.process.summary.service.conversion.IPDFConversionService;

@SpringBootTest
class PDFConversionServiceTest {

    @Autowired
    private IPDFConversionService conversionService;
    private byte[] pdfData;

    @BeforeEach
    void setUp() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("test.pdf")) {
            pdfData = is.readAllBytes();
        }

        assertNotNull(pdfData);

    }

    @Test
    void convert_WithRealPDF_Success() throws IOException {
        // Arrange
        FileEntity fileEntity = new FileEntity();
        fileEntity.setData(pdfData);

        // Act
        DocumentModel result = conversionService.convert(fileEntity);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getAuthor());
        assertNotNull(result.getTitle());
    }

    @Test
    void convertToPdDocument_WithRealPDF_Success() throws IOException {
        // Arrange
        FileEntity fileEntity = new FileEntity();
        fileEntity.setData(pdfData);

        // Act
        PDDocument result = conversionService.convertToPdDocument(fileEntity);

        // Assert
        assertNotNull(result);

        // Cleanup
        result.close();
    }

    @Test
    void convertToDocumentModel_WithRealPDF_Success() throws IOException {
        // Arrange
        PDDocument pdDoc = Loader.loadPDF(pdfData);

        // Act
        DocumentModel result = conversionService.convertToDocumentModel(pdDoc);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getTitle());
        assertNotNull(result.getAuthor());

        // Cleanup
        pdDoc.close();
    }
}
