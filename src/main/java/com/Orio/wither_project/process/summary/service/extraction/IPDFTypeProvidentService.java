package com.Orio.wither_project.process.summary.service.extraction;

import org.apache.pdfbox.pdmodel.PDDocument;

import com.Orio.wither_project.process.summary.model.PDFType;

public interface IPDFTypeProvidentService {

    PDFType getType(PDDocument doc);
}
