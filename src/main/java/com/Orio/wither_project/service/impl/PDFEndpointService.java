package com.Orio.wither_project.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import com.Orio.wither_project.model.PDFDocument;
import com.Orio.wither_project.service.data.managing.repoService.ISQLPDFService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PDFEndpointService {
    private static final Logger logger = LoggerFactory.getLogger(PDFEndpointService.class);
    private final ISQLPDFService sqlpdfService;

    @Transactional
    public boolean uploadPDF(MultipartFile file, String name) {
        if (file == null || file.isEmpty()) {
            logger.error("File or fileName is null");
            return false;
        }

        logger.debug("Processing PDF upload for file: {}", name);
        String givenName = (name == null || name.trim().isBlank()) ? file.getOriginalFilename() : name;

        if (givenName == null || givenName.trim().isEmpty()) {
            logger.error("File name is empty");
            return false;
        }

        // Add check for existing file name
        if (sqlpdfService.getDocumentByName(givenName).isPresent()) {
            logger.error("File with name {} already exists", givenName);
            return false;
        }

        PDFDocument pdfDocument = new PDFDocument();
        pdfDocument.setFileName(givenName);
        pdfDocument.setAuthor("test author");
        pdfDocument.setChapterSummaries(new ArrayList<>());

        try {
            pdfDocument.setData(file.getBytes());
        } catch (IOException e) {
            logger.error("Failed to read file data", e);
            return false;
        }

        pdfDocument.setPageSummaries(new ArrayList<>());
        pdfDocument.setSummary("test summary");

        try {
            return sqlpdfService.saveDocument(pdfDocument);
        } catch (Exception e) {
            logger.error("Failed to save document", e);
            return false;
        }
    }

    @Transactional(readOnly = true)
    public List<PDFDocument> getAllDocuments() {
        return sqlpdfService.getAllDocuments();
    }

    @Transactional(readOnly = true)
    public Optional<PDFDocument> getDocumentByName(String fileName) {
        return sqlpdfService.getDocumentByName(fileName);
    }
}
