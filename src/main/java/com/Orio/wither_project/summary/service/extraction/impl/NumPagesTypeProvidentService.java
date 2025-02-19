package com.Orio.wither_project.summary.service.extraction.impl;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.summary.model.PDFType;
import com.Orio.wither_project.summary.service.extraction.IPDFTypeProvidentService;

@Service
public class NumPagesTypeProvidentService implements IPDFTypeProvidentService {

    private static final Logger logger = LoggerFactory.getLogger(NumPagesTypeProvidentService.class);

    @Override
    public PDFType getType(PDDocument doc) {
        int numberOfPages = doc.getNumberOfPages();
        logger.info("Number of pages in the document: {}", numberOfPages);

        if (numberOfPages == 1) {
            logger.info("Document type determined: SHORT");
            return PDFType.SHORT;
        } else if (numberOfPages <= 15) {
            logger.info("Document type determined: MEDIUM");
            return PDFType.MEDIUM;
        } else {
            logger.info("Document type determined: LONG");
            return PDFType.LONG;
        }
    }

}
