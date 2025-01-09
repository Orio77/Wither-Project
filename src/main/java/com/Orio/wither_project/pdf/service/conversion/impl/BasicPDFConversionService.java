package com.Orio.wither_project.pdf.service.conversion.impl;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.pdf.model.ChapterModel;
import com.Orio.wither_project.pdf.model.DocumentModel;
import com.Orio.wither_project.pdf.repository.entity.FileEntity;
import com.Orio.wither_project.pdf.service.conversion.IPDFConversionService;
import com.Orio.wither_project.pdf.service.extraction.IPDFContentExtractionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BasicPDFConversionService implements IPDFConversionService {

    private static final Logger logger = LoggerFactory.getLogger(BasicPDFConversionService.class);

    private final IPDFContentExtractionService dataExtractionService;

    @Override
    public DocumentModel convertToDocumentModel(PDDocument pddoc) {
        logger.info("Starting conversion to DocumentModel");
        PDDocumentInformation info = pddoc.getDocumentInformation();

        String author = info.getAuthor();
        String title = info.getTitle();
        String fileName = info.getCustomMetadataValue("FileName");

        logger.debug("Extracted Author: {}", author);
        logger.debug("Extracted Title: {}", title);
        logger.debug("Extracted FileName: {}", fileName);

        DocumentModel doc = new DocumentModel();

        doc.setAuthor(author);
        doc.setTitle(title);
        doc.setFileName(fileName);

        logger.debug("DocumentModel Author set to: {}", author);
        logger.debug("DocumentModel Title set to: {}", title);
        logger.debug("DocumentModel FileName set to: {}", fileName);

        List<ChapterModel> chapters = dataExtractionService.getChapters(pddoc);
        doc.setChapters(chapters);

        logger.debug("Extracted Chapters: {}", chapters);

        logger.info("Conversion to DocumentModel completed successfully");
        return doc;
    }

    @Override
    public PDDocument convertToPdDocument(FileEntity file) throws IOException {
        logger.info("Starting conversion to PDDocument");
        try (PDDocument document = Loader.loadPDF(file.getData())) {
            logger.debug("PDDocument loaded successfully");
            logger.info("Conversion to PDDocument completed successfully");
            return document;
        } catch (IOException e) {
            logger.error("Error occurred during conversion to PDDocument", e);
            throw e;
        }
    }

}
