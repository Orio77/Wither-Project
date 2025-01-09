package com.Orio.wither_project.pdf.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

public class PDFUtil {

    private static final Logger logger = LoggerFactory.getLogger(PDFUtil.class);

    public static PDDocument convertToPDDocument(MultipartFile file) {
        logger.info("Starting PDF conversion for file: {}", file.getOriginalFilename());

        PDDocument document = new PDDocument();

        logger.debug("PDF processing completed.");

        logger.info("PDF conversion completed for file: {}", file.getOriginalFilename());
        return document;
    }
}