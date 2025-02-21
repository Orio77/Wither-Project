package com.Orio.wither_project.pdf.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.Orio.wither_project.constants.ApiPaths;
import com.Orio.wither_project.pdf.repository.entity.FileEntity;
import com.Orio.wither_project.pdf.service.storage.ISQLPDFService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PDFController {

    private static final Logger logger = LoggerFactory.getLogger(PDFController.class);

    private final ISQLPDFService sqlPDFService;

    @PostMapping(value = ApiPaths.BASE + ApiPaths.PDF_UPLOAD, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@RequestParam("pdf") MultipartFile pdf) {
        logger.info("Received request to upload PDF: {}", pdf.getOriginalFilename());

        if (pdf.isEmpty()) {
            logger.error("Received empty file");
            return ResponseEntity.badRequest().body("Empty file");
        }

        try {
            boolean saved = sqlPDFService.savePDF(pdf);
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

    @GetMapping(ApiPaths.BASE + ApiPaths.PDF_GET_FILE)
    public FileEntity getFile(@RequestParam(required = true) String name) { // TODO change to pdf
        logger.info("Received request to get PDF with name: {}", name);

        return sqlPDFService.getPDF(name);
    }

    @GetMapping(ApiPaths.BASE + ApiPaths.PDF_GET_FILE_ALL)
    public List<FileEntity> getAllFiles() { // TODO change to pdfs
        logger.info("Received request to get all PDFs");
        return sqlPDFService.getAllPDFs();
    }

}
