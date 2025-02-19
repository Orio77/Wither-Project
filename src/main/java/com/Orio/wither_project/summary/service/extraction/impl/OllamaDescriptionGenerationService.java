package com.Orio.wither_project.summary.service.extraction.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.summary.model.PDFType;
import com.Orio.wither_project.summary.service.extraction.IDescriptionGenerationService;

@Service
public class OllamaDescriptionGenerationService implements IDescriptionGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(OllamaDescriptionGenerationService.class);

    @Override
    public String generate(String text, PDFType type) {
        logger.info("Generating description for text: {} and PDFType: {}", text, type);
        String description = "A description of a text will be here";
        logger.debug("Generated description: {}", description);
        return description;
    }

}
