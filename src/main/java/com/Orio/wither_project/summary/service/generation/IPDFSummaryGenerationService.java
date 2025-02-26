package com.Orio.wither_project.summary.service.generation;

import java.util.List;

import com.Orio.wither_project.socket.summary.model.ProgressCallback;
import com.Orio.wither_project.summary.model.ChapterModel;
import com.Orio.wither_project.summary.model.ChapterSummaryModel;
import com.Orio.wither_project.summary.model.DocumentModel;
import com.Orio.wither_project.summary.model.DocumentSummaryModel;
import com.Orio.wither_project.summary.model.PageModel;
import com.Orio.wither_project.summary.model.PageSummaryModel;
import com.Orio.wither_project.summary.model.SummaryType;

public interface IPDFSummaryGenerationService {

    String summarize(String text, SummaryType type);

    String summarize(String text, String instruction);

    String summarizeProgressively(String text, SummaryType type);

    List<PageSummaryModel> generatePageSummaries(List<PageModel> pages);

    List<ChapterSummaryModel> generateChapterSummaries(List<ChapterModel> chapters, ProgressCallback progressCallback);

    DocumentSummaryModel generateDocumentSummary(DocumentModel documentModel, ProgressCallback progressCallback);
}
