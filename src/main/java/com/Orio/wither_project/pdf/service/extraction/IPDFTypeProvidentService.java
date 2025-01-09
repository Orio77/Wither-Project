package com.Orio.wither_project.pdf.service.extraction;

import org.apache.pdfbox.pdmodel.PDDocument;

import com.Orio.wither_project.pdf.model.PDFType;

public interface IPDFTypeProvidentService {

    PDFType getType(PDDocument doc);
}
