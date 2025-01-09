package com.Orio.wither_project.pdf.service.orchestration.impl;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.pdf.model.ChapterModel;
import com.Orio.wither_project.pdf.model.DocumentModel;
import com.Orio.wither_project.pdf.model.PageModel;
import com.Orio.wither_project.pdf.repository.entity.FileEntity;
import com.Orio.wither_project.pdf.service.conversion.IPDFConversionService;
import com.Orio.wither_project.pdf.service.extraction.IPDFContentExtractionService;
import com.Orio.wither_project.pdf.service.extraction.IPDFMetaDataExtractionService;
import com.Orio.wither_project.pdf.service.orchestration.IPDFProcessingOrchestrationService;
import com.Orio.wither_project.pdf.service.save.ISQLDocumentService;
import com.Orio.wither_project.pdf.summary.model.BookSummaryModel;
import com.Orio.wither_project.pdf.summary.model.ChapterSummaryModel;
import com.Orio.wither_project.pdf.summary.model.PageSummaryModel;
import com.Orio.wither_project.pdf.summary.service.IPDFSummaryGenerationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BasicPDFProcessingOrchestrationService implements IPDFProcessingOrchestrationService {

    private static final Logger logger = LoggerFactory.getLogger(BasicPDFProcessingOrchestrationService.class);

    @Autowired
    private final ISQLDocumentService pdfSavingService;
    @Autowired
    private final IPDFMetaDataExtractionService metaDataExtractionService;
    @Autowired
    private final IPDFSummaryGenerationService summaryGenerationService;
    @Autowired
    private final IPDFContentExtractionService contentExtractionService;
    @Autowired
    private final IPDFConversionService conversionService;

    @Override
    public DocumentModel convert(FileEntity file) throws IOException {
        logger.info("Starting PDF conversion for file: {}", file.getName());
        PDDocument doc = conversionService.convertToPdDocument(file);
        DocumentModel docModel = new DocumentModel();

        List<ChapterModel> chapters = contentExtractionService.getChapters(doc);
        docModel.setChapters(chapters);

        String author = metaDataExtractionService.getAuthor(doc);
        String title = metaDataExtractionService.getTitle(doc);
        String fileName = metaDataExtractionService.getFileName(doc);
        docModel.setAuthor(author);
        docModel.setTitle(title);
        docModel.setFileName(fileName);

        doc.close();
        logger.info("PDF conversion completed for file: {}", file.getName());
        return docModel;
    }

    @Override
    public boolean setMetadata(DocumentModel model) {
        logger.info("Setting metadata for document: {}", model.getFileName());
        return true;
    }

    @Override
    public boolean setContents(DocumentModel model) {
        try {
            logger.info("Setting contents for document: {}", model.getFileName());
            List<ChapterModel> chapters = model.getChapters();
            List<PageModel> pages = chapters.stream()
                    .flatMap(chapter -> chapter.getPages().stream())
                    .toList();

            pdfSavingService.savePages(pages);
            pdfSavingService.saveChapters(chapters);
            logger.info("Contents set successfully for document: {}", model.getFileName());
            return true;
        } catch (Exception e) {
            logger.error("Error setting contents for document: {}", model.getFileName(), e);
            return false;
        }
    }

    @Override
    public boolean setSummaries(DocumentModel model) {
        try {
            logger.info("Setting summaries for document: {}", model.getFileName());
            List<ChapterModel> chapters = model.getChapters();
            generateAndSavePageSummaries(chapters);
            generateAndSaveChapterSummaries(chapters);
            generateAndSaveBookSummary(model, chapters);
            logger.info("Summaries set successfully for document: {}", model.getFileName());
            return true;
        } catch (Exception e) {
            logger.error("Error setting summaries for document: {}", model.getFileName(), e);
            return false;
        }
    }

    private void generateAndSavePageSummaries(List<ChapterModel> chapters) {
        logger.info("Generating and saving page summaries");
        List<PageModel> pages = chapters.stream().flatMap(chapter -> chapter.getPages().stream()).toList();
        pages.forEach(page -> {
            String pageSummary = summaryGenerationService.summarizePage(page.getContent());
            PageSummaryModel pageSummaryModel = new PageSummaryModel(pageSummary);
            pageSummaryModel.setPage(page);
            page.setSummary(pageSummaryModel);
        });

        pdfSavingService.savePages(pages);
        logger.info("Page summaries generated and saved successfully");
    }

    private void generateAndSaveChapterSummaries(List<ChapterModel> chapters) {
        logger.info("Generating and saving chapter summaries");
        chapters.forEach(chapter -> {
            String pageSummaries = chapter.getPages().stream()
                    .map(PageModel::getSummary)
                    .map(PageSummaryModel::getContent)
                    .collect(Collectors.joining("\n"));
            String chapterSummary = summaryGenerationService.summarizeChapter(pageSummaries);
            ChapterSummaryModel chapterSummaryModel = new ChapterSummaryModel(chapterSummary);
            chapterSummaryModel.setChapter(chapter);
            chapter.setSummary(chapterSummaryModel);
        });

        pdfSavingService.saveChapters(chapters);
        logger.info("Chapter summaries generated and saved successfully");
    }

    private void generateAndSaveBookSummary(DocumentModel model, List<ChapterModel> chapters) {
        logger.info("Generating and saving book summary for document: {}", model.getFileName());
        String chapterSummaries = chapters.stream()
                .map(ChapterModel::getSummary)
                .map(ChapterSummaryModel::getContent)
                .collect(Collectors.joining("\n"));
        String bookSummary = summaryGenerationService.summarizeDocument(chapterSummaries);
        BookSummaryModel bookSummaryModel = new BookSummaryModel(bookSummary);
        model.setSummary(bookSummaryModel);

        pdfSavingService.saveDoc(model);
        logger.info("Book summary generated and saved successfully for document: {}", model.getFileName());
    }
}
