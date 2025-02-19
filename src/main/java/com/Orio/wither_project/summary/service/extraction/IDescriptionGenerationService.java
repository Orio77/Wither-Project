package com.Orio.wither_project.summary.service.extraction;

import com.Orio.wither_project.summary.model.PDFType;

public interface IDescriptionGenerationService {

    String generate(String text, PDFType type);
}
