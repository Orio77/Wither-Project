package com.Orio.wither_project.process.summary.service.extraction;

import org.apache.pdfbox.pdmodel.PDDocument;

public interface IPDFMetaDataExtractionService {

    String getAuthor(PDDocument doc);

    String getTitle(PDDocument doc);

    String getFileName(PDDocument doc);
}
