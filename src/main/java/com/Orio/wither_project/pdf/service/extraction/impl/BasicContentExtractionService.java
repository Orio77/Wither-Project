package com.Orio.wither_project.pdf.service.extraction.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.pdf.model.ChapterModel;
import com.Orio.wither_project.pdf.model.PageModel;
import com.Orio.wither_project.pdf.service.extraction.IPDFContentExtractionService;

@Service
public class BasicContentExtractionService implements IPDFContentExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(BasicContentExtractionService.class);

    @Override
    public List<PageModel> getPages(PDDocument doc) {
        logger.info("Extracting pages from document");
        return new ArrayList<>();
    }

    @Override
    public List<ChapterModel> getChapters(PDDocument doc) {
        logger.info("Extracting chapters from document");
        return new ArrayList<>();
    }

}
