package com.Orio.wither_project.summary.service.conversion.impl;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.pdf.model.entity.FileEntity;
import com.Orio.wither_project.summary.config.SummaryConstantsConfig;
import com.Orio.wither_project.summary.model.ChapterModel;
import com.Orio.wither_project.summary.model.DocumentModel;
import com.Orio.wither_project.summary.service.conversion.IPDFConversionService;
import com.Orio.wither_project.summary.service.extraction.IPDFContentExtractionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BasicPDFConversionService implements IPDFConversionService {

    private static final Logger logger = LoggerFactory.getLogger(BasicPDFConversionService.class);

    private final IPDFContentExtractionService contentExtractionService;
    private final SummaryConstantsConfig constantsConfig;

    @Override
    public PDDocument convertToPdDocument(FileEntity file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("FileEntity cannot be null");
        }

        byte[] data = file.getData();
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("File data cannot be null or empty");
        }

        String fileName = file.getFileName();
        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = file.getName(); // Fallback to name if fileName is not available
            logger.warn("File fileName is empty, using name: {}", fileName);
        }

        String name = file.getName();
        if (name == null || name.trim().isEmpty()) {
            name = fileName; // Fallback to fileName if name is not available
            logger.warn("File name is empty, using fileName: {}", name);
        }

        logger.info("Converting file {} to PDDocument", name);
        try {
            PDDocument document = Loader.loadPDF(data);
            PDDocumentInformation info = new PDDocumentInformation();
            info.setTitle(name);
            info.setCustomMetadataValue(constantsConfig.getFileName(), fileName);
            document.setDocumentInformation(info);

            logger.info("Successfully converted file to PDDocument");
            return document;
        } catch (IOException e) {
            logger.error("Failed to convert file to PDDocument: {}", e.getMessage());
            throw new IOException("Failed to convert file to PDDocument", e);
        }
    }

    @Override
    public DocumentModel convertToDocumentModel(PDDocument pddoc) {
        logger.info("Starting PDF document conversion to DocumentModel");
        if (pddoc == null) {
            throw new IllegalArgumentException("PDDocument cannot be null");
        }

        PDDocumentInformation info = pddoc.getDocumentInformation();
        if (info == null) {
            info = new PDDocumentInformation();
        }

        String author = info.getAuthor();
        String title = info.getTitle();
        String fileName = info.getCustomMetadataValue(constantsConfig.getFileName());

        logger.debug("Extracted metadata - Author: {}, Title: {}, FileName: {}", author, title, fileName);

        DocumentModel doc = new DocumentModel();
        doc.setAuthor(author);
        doc.setTitle(title);
        doc.setFileName(fileName);

        List<ChapterModel> chapters = contentExtractionService.getChapters(pddoc);
        doc.setChapters(chapters);

        logger.info("PDF document conversion completed successfully");
        return doc;
    }

}
