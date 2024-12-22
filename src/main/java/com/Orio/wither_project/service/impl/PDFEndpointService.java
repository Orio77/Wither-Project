package com.Orio.wither_project.service.impl;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.Orio.wither_project.model.BookModel;
import com.Orio.wither_project.service.data.managing.repoService.ISQLPDFService;
import com.Orio.wither_project.service.data.processing.book.IAIBookProcessingService;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

@Service
@RequiredArgsConstructor
public class PDFEndpointService {
    private static final Logger logger = LoggerFactory.getLogger(PDFEndpointService.class);
    private final ISQLPDFService sqlpdfService;
    private final IAIBookProcessingService bookProcessingService;

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

        if (sqlpdfService.getDocumentByName(givenName).isPresent()) {
            logger.error("File with name {} already exists", givenName);
            return false;
        }

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            if (document.getNumberOfPages() == 0) {
                logger.error("PDF document is empty");
                return false;
            }

            BookModel pdfDocument = bookProcessingService.processPDFDocument(document, givenName);
            return sqlpdfService.saveDocument(pdfDocument);
        } catch (Exception e) {
            logger.error("Failed to process and save document", e);
            return false;
        }
    }

    @Transactional(readOnly = true)
    public List<BookModel> getAllDocuments() {
        return sqlpdfService.getAllDocuments();
    }

    @Transactional(readOnly = true)
    public Optional<BookModel> getDocumentByName(String fileName) {
        return sqlpdfService.getDocumentByName(fileName);
    }
}
