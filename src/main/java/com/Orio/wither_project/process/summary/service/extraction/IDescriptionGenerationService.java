package com.Orio.wither_project.process.summary.service.extraction;

import com.Orio.wither_project.process.summary.model.PDFType;

public interface IDescriptionGenerationService {

    String generate(String text, PDFType type);
}
