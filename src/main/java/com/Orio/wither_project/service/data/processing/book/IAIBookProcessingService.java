package com.Orio.wither_project.service.data.processing.book;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;

import com.Orio.wither_project.pdf.model.ChapterModel;
import com.Orio.wither_project.pdf.model.DocumentModel;
import com.Orio.wither_project.pdf.model.PageModel;
import com.Orio.wither_project.pdf.summary.model.BookSummaryModel;
import com.Orio.wither_project.pdf.summary.model.ChapterSummaryModel;
import com.Orio.wither_project.pdf.summary.model.PageSummaryModel;

public interface IAIBookProcessingService {

    BookSummaryModel getBookSummary(List<ChapterSummaryModel> chapterSummaries);

    List<ChapterModel> getChapters(PDDocument doc, List<Integer> startPages) throws IOException;

    ChapterSummaryModel generateChapterSummary(List<PageSummaryModel> pageSummaries);

    List<PageSummaryModel> getPageSummaries(List<PageModel> pages);

    DocumentModel processPDFDocument(PDDocument doc, String fileName, List<Integer> startChapterPages)
            throws IOException;

    List<PageModel> getPages(PDDocument doc) throws IOException;
}
