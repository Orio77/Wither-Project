package com.Orio.wither_project.pdf.summary.service;

import java.util.List;

import com.Orio.wither_project.pdf.model.ChapterModel;
import com.Orio.wither_project.pdf.model.DocumentModel;
import com.Orio.wither_project.pdf.model.PageModel;
import com.Orio.wither_project.pdf.summary.model.BookSummaryModel;
import com.Orio.wither_project.pdf.summary.model.ChapterSummaryModel;
import com.Orio.wither_project.pdf.summary.model.PageSummaryModel;

public interface IPDFParallelSummaryGenerationService {

    List<PageSummaryModel> generatePageSummaries(List<PageModel> pages);

    List<ChapterSummaryModel> generateChapterSummaries(List<ChapterModel> chapters);

    BookSummaryModel generateBookSummary(DocumentModel doc);
}
