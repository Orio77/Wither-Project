package com.Orio.wither_project.pdf.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.Orio.wither_project.pdf.dto.ProcessRequestDTO;
import com.Orio.wither_project.pdf.repository.entity.FileEntity;
import com.Orio.wither_project.pdf.service.orchestration.IPDFProcessingOrchestrationService;
import com.Orio.wither_project.pdf.service.save.ISQLDocumentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PDFController {

    private static final Logger logger = LoggerFactory.getLogger(PDFController.class);

    @Autowired
    private final ISQLDocumentService pdfSavingService;
    @Autowired
    private final IPDFProcessingOrchestrationService processingService;

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestBody MultipartFile pdf) {
        logger.info("Received request to upload PDF");
        boolean saved = pdfSavingService.savePDF(pdf);

        if (saved) {
            logger.info("File uploaded successfully");
            return ResponseEntity.ok("File uploaded successfully");
        } else {
            logger.error("Failed to upload file");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file");
        }
    }

    @PostMapping("/process")
    public ResponseEntity<String> process(@RequestBody ProcessRequestDTO request) {
        logger.info("Received request to process PDF with name: {}", request.getName());
        try {
            FileEntity file = get(request.getName());
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

    @GetMapping("/get")
    public FileEntity get(@RequestParam(required = true) String name) {
        logger.info("Received request to get PDF with name: {}", name);

        return pdfSavingService.getPDF(name);
    }

    @GetMapping("get/all")
    public List<MultipartFile> getAll() {
        logger.info("Received request to get all PDFs");
        return new ArrayList<>();
    }

}
