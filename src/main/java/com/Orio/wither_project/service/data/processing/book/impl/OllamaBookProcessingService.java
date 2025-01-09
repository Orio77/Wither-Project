// package com.Orio.wither_project.service.data.processing.book.impl;

// import java.io.IOException;
// import java.util.List;
// import java.util.stream.Collectors;

// import org.apache.pdfbox.pdmodel.PDDocument;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.stereotype.Service;

// import com.Orio.wither_project.model.*;
// import com.Orio.wither_project.pdf.model.ChapterModel;
// import com.Orio.wither_project.pdf.model.DocumentModel;
// import com.Orio.wither_project.pdf.model.PageModel;
// import com.Orio.wither_project.pdf.summary.model.BookSummaryModel;
// import com.Orio.wither_project.pdf.summary.model.ChapterSummaryModel;
// import com.Orio.wither_project.pdf.summary.model.PageSummaryModel;
// import
// com.Orio.wither_project.service.data.processing.book.IAIBookProcessingService;
// import
// com.Orio.wither_project.service.data.processing.book.extractor.IBookDataExtractor;
// import com.Orio.wither_project.service.summary.IBookSummaryService;

// import lombok.RequiredArgsConstructor;

// @Service
// @RequiredArgsConstructor
// public class OllamaBookProcessingService implements IAIBookProcessingService
// {
// private static final Logger logger =
// LoggerFactory.getLogger(OllamaBookProcessingService.class);
// private final IBookDataExtractor bookDataExtractor;
// private final IBookSummaryService bookSummaryService;

// @Override
// public DocumentModel processPDFDocument(PDDocument doc, String fileName,
// List<Integer> startChapterPages)
// throws IOException {
// logger.info("Starting to process PDF document: {}", fileName);

// DocumentModel bookModel = new DocumentModel();
// List<ChapterModel> chapters = getChapters(doc, startChapterPages);

// for (ChapterModel chapter : chapters) {
// chapter.setBook(bookModel);
// setChapterContent(chapter);
// }

// bookModel.setTitle(bookDataExtractor.extractTitle(doc));
// bookModel.setAuthor(bookDataExtractor.extractAuthor(doc));
// bookModel.setChapters(chapters);

// processChapterSummaries(chapters, bookModel);

// logger.info("Completed processing PDF document: {}", fileName);
// return bookModel;
// }

// private void setChapterContent(ChapterModel chapter) {
// logger.debug("Setting content for chapter: {}", chapter.getTitle());
// String chapterContent = chapter.getPages().stream()
// .map(PageModel::getContent)
// .filter(content -> content != null && !content.trim().isEmpty())
// .collect(Collectors.joining("\n"));
// chapter.setContent(chapterContent);
// logger.debug("Content set for chapter: {}, content length: {}",
// chapter.getTitle(), chapterContent.length());
// }

// private void processChapterSummaries(List<ChapterModel> chapters,
// DocumentModel bookModel) {
// logger.debug("Starting chapter summaries generation for {} chapters",
// chapters.size());
// List<ChapterSummaryModel> chapterSummaries = chapters.stream()
// .map(this::processChapterSummary)
// .collect(Collectors.toList());

// logger.debug("Generating book summary from {} chapter summaries",
// chapterSummaries.size());
// BookSummaryModel bookSummary = getBookSummary(chapterSummaries);
// bookSummary.setBook(bookModel);
// bookModel.setSummary(bookSummary);
// logger.debug("Book summary generation completed");
// }

// private ChapterSummaryModel processChapterSummary(ChapterModel chapter) {
// logger.debug("Processing summary for chapter: {} with {} pages",
// chapter.getTitle(), chapter.getPages().size());
// var pageSummaries = getPageSummaries(chapter.getPages());
// logger.debug("Generated {} page summaries for chapter: {}",
// pageSummaries.size(), chapter.getTitle());
// var summary = generateChapterSummary(pageSummaries);
// chapter.setSummary(summary);
// logger.debug("Chapter summary generated for: {}", chapter.getTitle());
// return summary;
// }

// @Override
// public List<ChapterModel> getChapters(PDDocument doc, List<Integer>
// startPages) throws IOException {
// return bookDataExtractor.getChapters(doc, startPages);
// }

// @Override
// public List<PageSummaryModel> getPageSummaries(List<PageModel> pages) {
// logger.debug("Generating summaries for {} pages", pages.size());
// List<PageSummaryModel> summaries =
// bookSummaryService.getPageSummaries(pages);
// logger.debug("Generated {} page summaries", summaries.size());
// return summaries;
// }

// @Override
// public ChapterSummaryModel generateChapterSummary(List<PageSummaryModel>
// pageSummaries) {
// return bookSummaryService.generateChapterSummary(pageSummaries);
// }

// @Override
// public BookSummaryModel getBookSummary(List<ChapterSummaryModel>
// chapterSummaries) {
// return bookSummaryService.getBookSummary(chapterSummaries);
// }

// @Override
// public List<PageModel> getPages(PDDocument doc) throws IOException {
// return bookDataExtractor.getPages(doc);
// }
// }