package com.Orio.wither_project.service.data.processing.book;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;

import com.Orio.wither_project.model.BookModel;
import com.Orio.wither_project.model.BookSummaryModel;
import com.Orio.wither_project.model.ChapterModel;
import com.Orio.wither_project.model.ChapterSummaryModel;
import com.Orio.wither_project.model.PageModel;
import com.Orio.wither_project.model.PageSummaryModel;

public interface IAIBookProcessingService {

    BookSummaryModel getBookSummary(List<ChapterSummaryModel> chapterSummaries);

    List<ChapterModel> getChapters(PDDocument doc) throws IOException;

    ChapterSummaryModel generateChapterSummary(List<PageSummaryModel> pageSummaries);

    List<PageSummaryModel> getPageSummaries(List<PageModel> pages);

    BookModel processPDFDocument(PDDocument doc, String fileName) throws IOException;
}
