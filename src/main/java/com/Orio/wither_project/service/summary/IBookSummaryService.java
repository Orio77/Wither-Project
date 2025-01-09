// package com.Orio.wither_project.service.summary;

// import java.util.List;
// import com.Orio.wither_project.model.*;

// public interface IBookSummaryService {
// /**
// * Creates a summary for a single piece of content
// */
// <T> T createSummary(String content, ISummaryType type);

// /**
// * Creates a progressive summary from a list of summarizable content
// */
// <T, E extends ProgressivelySummarizable<T>> E createProgressiveSummary(
// List<T> parts,
// ISummaryType type,
// E container);

// /**
// * Creates summaries for a list of pages
// */
// List<PageSummaryModel> getPageSummaries(List<PageModel> pages);

// /**
// * Generates a chapter summary from page summaries
// */
// ChapterSummaryModel generateChapterSummary(List<PageSummaryModel>
// pageSummaries);

// /**
// * Creates a book summary from chapter summaries
// */
// BookSummaryModel getBookSummary(List<ChapterSummaryModel> chapterSummaries);
// }
