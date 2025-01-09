package com.Orio.wither_project.service.data.processing.book.extractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.pdf.model.ChapterModel;
import com.Orio.wither_project.pdf.model.PageModel;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PDFBookDataExtractor implements IBookDataExtractor {
    private static final Logger logger = LoggerFactory.getLogger(PDFBookDataExtractor.class);
    private final ChapterExtractionService chapterExtractor;

    @Override
    public List<ChapterModel> getChapters(PDDocument document, List<Integer> startPages) throws IOException {
        logger.debug("Extracting chapters from document");
        List<ChapterModel> chapters = chapterExtractor.getChapters(document, startPages);
        logger.debug("Extracted {} chapters from document", chapters.size());
        return chapters;
    }

    @Override
    public List<PageModel> getPages(PDDocument document) throws IOException {
        logger.debug("Extracting pages from document");
        List<PageModel> pages = new ArrayList<>();
        PDFTextStripper stripper = new PDFTextStripper();

        for (int i = 0; i < document.getNumberOfPages(); i++) {
            stripper.setStartPage(i + 1);
            stripper.setEndPage(i + 1);
            String pageText = stripper.getText(document);

            PageModel page = new PageModel();
            page.setContent(pageText);
            page.setPageNumber(i + 1);
            pages.add(page);
        }

        logger.debug("Extracted {} pages from document", pages.size());
        return pages;
    }

    @Override
    public String extractAuthor(PDDocument document) {
        logger.debug("Extracting author from document metadata");
        PDDocumentInformation info = document.getDocumentInformation();
        String author = info != null && info.getAuthor() != null ? info.getAuthor() : "Unknown Author";
        logger.debug("Extracted author: {}", author);
        return author;
    }

    @Override
    public String extractTitle(PDDocument document) {
        logger.debug("Extracting title from document metadata");
        PDDocumentInformation info = document.getDocumentInformation();
        String title = info != null && info.getTitle() != null ? info.getTitle() : "Untitled";
        logger.debug("Extracted title: {}", title);
        return title;
    }
}
