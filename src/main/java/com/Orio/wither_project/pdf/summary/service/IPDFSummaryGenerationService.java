package com.Orio.wither_project.pdf.summary.service;

import com.Orio.wither_project.pdf.model.SummaryType;

public interface IPDFSummaryGenerationService {

    String summarize(String text, SummaryType type);

    String summarize(String text, String instruction);

    default String summarizePage(String text) {
        return summarize(text, SummaryType.PAGE);
    }

    default String summarizeChapter(String text) {
        return summarize(text, SummaryType.CHAPTER);
    }

    default String summarizeDocument(String text) {
        return summarize(text, SummaryType.BOOK);
    }
}
