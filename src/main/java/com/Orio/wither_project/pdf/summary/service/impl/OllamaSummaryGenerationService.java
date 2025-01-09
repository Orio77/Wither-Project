package com.Orio.wither_project.pdf.summary.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.pdf.model.SummaryType;
import com.Orio.wither_project.pdf.summary.service.IPDFSummaryGenerationService;

@Service
public class OllamaSummaryGenerationService implements IPDFSummaryGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(OllamaSummaryGenerationService.class);

    @Override
    public String summarize(String text, SummaryType type) {
        logger.info("Summarizing text with SummaryType: {}", type);
        // Add your summary generation logic here
        return "A summary of the text will be here";
    }

    @Override
    public String summarize(String text, String instruction) {
        logger.info("Summarizing text with instruction: {}", instruction);
        // Add your summary generation logic here
        return "A summary of the text will be here";
    }

}
