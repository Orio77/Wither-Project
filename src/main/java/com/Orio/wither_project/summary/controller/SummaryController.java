package com.Orio.wither_project.summary.controller;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Orio.wither_project.constants.ApiPaths;
import com.Orio.wither_project.pdf.model.entity.FileEntity;
import com.Orio.wither_project.pdf.service.storage.ISQLPDFService;
import com.Orio.wither_project.summary.model.DocumentModel;
import com.Orio.wither_project.summary.model.ProcessingProgressModel;
import com.Orio.wither_project.summary.model.dto.ProcessRequestDTO;
import com.Orio.wither_project.summary.service.orchestration.IPDFProcessingOrchestrationService;
import com.Orio.wither_project.summary.service.progress.ProcessingProgressService;
import com.Orio.wither_project.summary.service.storage.ISQLDocumentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SummaryController {

    private static final Logger logger = LoggerFactory.getLogger(SummaryController.class);

    private final ISQLDocumentService sqlDocumentService;
    private final ISQLPDFService sqlPDFService;
    private final IPDFProcessingOrchestrationService processingService;
    private final ProcessingProgressService processingProgressService;

    @PostMapping(ApiPaths.BASE + ApiPaths.PDF_PROCESS)
    public ResponseEntity<String> process(@RequestBody ProcessRequestDTO request) {
        logger.info("Received request to process PDF with name: {}, restart: {}",
                request.getName(), request.isRestart());
        try {
            FileEntity file = sqlPDFService.getPDF(request.getName());
            if (file == null) {
                logger.error("File not found with name: {}", request.getName());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
            }

            if (request.isRestart()) {
                processingProgressService.resetProgress(request.getName());
                logger.info("Reset processing progress for file: {}", request.getName());
            }

            boolean success = processingService.continueProcessingPDF(file);

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

    @GetMapping(ApiPaths.BASE + ApiPaths.PDF_GET_DOC)
    public DocumentModel getDoc(@RequestParam(required = true) String name) {
        logger.info("Received request to get document with name: {}", name);

        DocumentModel doc = sqlDocumentService.getDocument(name);
        logger.info("Retrieved document: {}", doc);
        logger.info("Number of chapters: {}", doc != null ? doc.getChapters().size() : 0);

        return doc;
    }

    @GetMapping(ApiPaths.BASE + ApiPaths.PDF_GET_DOC_ALL)
    public List<DocumentModel> getAllDocs() {
        logger.info("Received request to get all PDFs");
        return sqlDocumentService.getAllDocs();
    }

    @DeleteMapping(ApiPaths.BASE + ApiPaths.PDF_DELETE_DOC)
    public ResponseEntity<Void> deleteDoc(@RequestParam(required = true) String name) {
        logger.info("Received request to delete document with name: {}", name);
        sqlDocumentService.deleteDoc(name);
        logger.info("Document deleted successfully: {}", name);
        return ResponseEntity.ok().build();
    }

    @GetMapping(ApiPaths.BASE + ApiPaths.PDF_PROGRESS)
    public ResponseEntity<ProcessingProgressModel> getProcessingProgress(@RequestParam String name) {
        logger.info("Received request to get processing progress for: {}", name);

        return processingProgressService.getProgress(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
