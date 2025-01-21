package com.Orio.wither_project.pdf.controller;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.Orio.wither_project.constants.ApiPaths;
import com.Orio.wither_project.pdf.model.DocumentModel;
import com.Orio.wither_project.pdf.model.dto.ProcessRequestDTO;
import com.Orio.wither_project.pdf.repository.entity.FileEntity;
import com.Orio.wither_project.pdf.service.orchestration.IPDFProcessingOrchestrationService;
import com.Orio.wither_project.pdf.service.storage.ISQLDocumentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PDFController {

    private static final Logger logger = LoggerFactory.getLogger(PDFController.class);

    private final ISQLDocumentService sqlDocumentService;
    private final IPDFProcessingOrchestrationService processingService;

    @PostMapping(value = ApiPaths.BASE + ApiPaths.PDF_UPLOAD, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@RequestParam("pdf") MultipartFile pdf) {
        logger.info("Received request to upload PDF: {}", pdf.getOriginalFilename());

        if (pdf.isEmpty()) {
            logger.error("Received empty file");
            return ResponseEntity.badRequest().body("Empty file");
        }

        try {
            boolean saved = sqlDocumentService.savePDF(pdf);
            if (saved) {
                logger.info("File uploaded successfully");
                return ResponseEntity.ok("File uploaded successfully");
            } else {
                logger.error("Failed to upload file");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file");
            }
        } catch (Exception e) {
            logger.error("Error uploading file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading file: " + e.getMessage());
        }
    }

    @PostMapping(ApiPaths.BASE + ApiPaths.PDF_PROCESS)
    public ResponseEntity<String> process(@RequestBody ProcessRequestDTO request) {
        logger.info("Received request to process PDF with name: {}", request.getName());
        try {
            FileEntity file = getFile(request.getName());
            if (file == null) {
                logger.error("File not found with name: {}", request.getName());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
            }

            boolean success = processingService.processPDF(file);

            if (success) {
                logger.info("PDF processed successfully");
                return ResponseEntity.ok("PDF processed successfully");
            } else {
                logger.error("Failed to process PDF");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process PDF");
            }
        } catch (IOException e) {
            logger.error("Error processing PDF: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing PDF: " + e.getMessage());
        }
    }

    @GetMapping(ApiPaths.BASE + ApiPaths.PDF_GET_FILE)
    public FileEntity getFile(@RequestParam(required = true) String name) {
        logger.info("Received request to get PDF with name: {}", name);

        return sqlDocumentService.getPDF(name);
    }

    @GetMapping(ApiPaths.BASE + ApiPaths.PDF_GET_FILE_ALL)
    public List<FileEntity> getAllFiles() { // Change return type to match frontend expectations
        logger.info("Received request to get all PDFs");
        return sqlDocumentService.getAllPDFs(); // Implement this method in your service
    }

    @GetMapping(ApiPaths.BASE + ApiPaths.PDF_GET_DOC)
    public DocumentModel getDoc(@RequestParam(required = true) String name) {
        logger.info("Received request to get document with name: {}", name);

        DocumentModel doc = sqlDocumentService.getDocument(name);
        logger.info("Retrieved document: {}", doc);
        logger.info("Number of chapters: {}", doc != null ? doc.getChapters().size() : 0);

        return doc;
    }

    @GetMapping(ApiPaths.BASE + ApiPaths.PDF_GET_DOC_ALL)
    public List<DocumentModel> getAllDocs() { // Change return type to match frontend expectations
        logger.info("Received request to get all PDFs");
        return sqlDocumentService.getAllDocs(); // Implement this method in your service
    }

}
