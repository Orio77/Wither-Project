package com.Orio.wither_project.pdf.service.extraction.impl;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.pdf.service.extraction.IPDFMetaDataExtractionService;

@Service
public class ApachePDFBoxMetaDataExtractionService implements IPDFMetaDataExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(ApachePDFBoxMetaDataExtractionService.class);

    @Override
    public String getAuthor(PDDocument doc) {
        String author = doc.getDocumentInformation().getAuthor();
        logger.info("Extracted author: {}", author);
        return author;
    }

    @Override
    public String getTitle(PDDocument doc) {
        String title = doc.getDocumentInformation().getTitle();
        logger.info("Extracted title: {}", title);
        return title;
    }

    @Override
    public String getFileName(PDDocument doc) {
        String fileName = doc.getDocumentInformation().getCustomMetadataValue("FileName"); // TODO Make this a const
        logger.info("Extracted file name: {}", fileName);
        return fileName;
    }

}
