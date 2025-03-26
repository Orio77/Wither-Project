package com.Orio.wither_project.process.summary.service.generation;

import java.util.List;

import com.Orio.wither_project.process.summary.model.ChapterModel;
import com.Orio.wither_project.process.summary.model.ChapterSummaryModel;
import com.Orio.wither_project.process.summary.model.DocumentModel;
import com.Orio.wither_project.process.summary.model.DocumentSummaryModel;
import com.Orio.wither_project.process.summary.model.PageModel;
import com.Orio.wither_project.process.summary.model.PageSummaryModel;

public interface IPDFParallelSummaryGenerationService {

    List<PageSummaryModel> generatePageSummaries(List<PageModel> pages);

    List<ChapterSummaryModel> generateChapterSummaries(List<ChapterModel> chapters);

    DocumentSummaryModel generateBookSummary(DocumentModel doc);
}
