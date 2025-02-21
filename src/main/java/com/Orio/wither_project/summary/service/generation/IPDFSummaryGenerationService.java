package com.Orio.wither_project.summary.service.generation;

import java.util.List;

import com.Orio.wither_project.summary.model.PageModel;
import com.Orio.wither_project.summary.model.PageSummaryModel;
import com.Orio.wither_project.summary.model.SummaryType;

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
