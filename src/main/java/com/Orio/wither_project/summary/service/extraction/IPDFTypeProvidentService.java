package com.Orio.wither_project.summary.service.extraction;

import org.apache.pdfbox.pdmodel.PDDocument;

import com.Orio.wither_project.summary.model.PDFType;

public interface IPDFTypeProvidentService {

    PDFType getType(PDDocument doc);
}
