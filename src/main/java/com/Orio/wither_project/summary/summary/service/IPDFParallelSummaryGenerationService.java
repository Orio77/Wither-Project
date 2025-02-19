package com.Orio.wither_project.summary.summary.service;

import java.util.List;

import com.Orio.wither_project.summary.model.ChapterModel;
import com.Orio.wither_project.summary.model.DocumentModel;
import com.Orio.wither_project.summary.model.PageModel;
import com.Orio.wither_project.summary.summary.model.BookSummaryModel;
import com.Orio.wither_project.summary.summary.model.ChapterSummaryModel;
import com.Orio.wither_project.summary.summary.model.PageSummaryModel;

public interface IPDFParallelSummaryGenerationService {

    List<PageSummaryModel> generatePageSummaries(List<PageModel> pages);

    List<ChapterSummaryModel> generateChapterSummaries(List<ChapterModel> chapters);

    BookSummaryModel generateBookSummary(DocumentModel doc);
}
