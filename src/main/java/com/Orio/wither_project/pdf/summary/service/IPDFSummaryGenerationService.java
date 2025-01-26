package com.Orio.wither_project.pdf.summary.service;

import com.Orio.wither_project.pdf.summary.model.SummaryType;
import java.util.List;
import com.Orio.wither_project.pdf.model.PageModel;
import com.Orio.wither_project.pdf.summary.model.PageSummaryModel;

public interface IPDFSummaryGenerationService {

    String summarize(String text, SummaryType type);

    String summarize(String text, String instruction, String responseFormat);

    String summarize(String text, String instruction);

    String summarizeProgressively(String text, SummaryType type);

    default String summarizePage(String text) {
        return summarize(text, SummaryType.PAGE);
    }

    default String summarizeChapter(String text) {
        return summarizeProgressively(text, SummaryType.CHAPTER);
    }

    default String summarizeDocument(String text) {
        return summarizeProgressively(text, SummaryType.BOOK);
    }

    List<PageSummaryModel> generatePageSummaries(List<PageModel> pages);
}
