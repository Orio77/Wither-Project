package com.Orio.wither_project.process.summary.service.generation;

import java.util.List;

import com.Orio.wither_project.process.summary.model.ChapterModel;
import com.Orio.wither_project.process.summary.model.ChapterSummaryModel;
import com.Orio.wither_project.process.summary.model.DocumentModel;
import com.Orio.wither_project.process.summary.model.DocumentSummaryModel;
import com.Orio.wither_project.process.summary.model.PageModel;
import com.Orio.wither_project.process.summary.model.PageSummaryModel;
import com.Orio.wither_project.process.summary.model.SummaryType;
import com.Orio.wither_project.socket.summary.model.ProgressCallback;

public interface IPDFSummaryGenerationService {

    String summarize(String text, SummaryType type);

    String summarize(String text, String instruction);

    String summarizeProgressively(String text, SummaryType type);

    PageSummaryModel generatePageSummary(PageModel page);

    List<PageSummaryModel> generatePageSummaries(List<PageModel> pages);

    List<ChapterSummaryModel> generateChapterSummaries(List<ChapterModel> chapters, ProgressCallback progressCallback);

    DocumentSummaryModel generateDocumentSummary(DocumentModel documentModel, ProgressCallback progressCallback);
}
