package com.Orio.wither_project.pdf.service.extraction;

import com.Orio.wither_project.pdf.model.PDFType;

public interface IDescriptionGenerationService {

    String generate(String text, PDFType type);
}
