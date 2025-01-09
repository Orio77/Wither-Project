// package com.Orio.wither_project.service.summary;

// import com.Orio.wither_project.model.AbstractSummaryModel;
// import com.Orio.wither_project.model.PageSummaryModel;
// import com.Orio.wither_project.model.ChapterSummaryModel;
// import com.Orio.wither_project.model.BookSummaryModel;

// public enum SummaryType implements ISummaryType {
// PAGE {
// @Override
// public String getSystemPrompt() {
// return "You are a helpful assistant. Create a concise summary of this page.";
// }

// @Override
// public AbstractSummaryModel createSummary(String content) {
// return new PageSummaryModel(content);
// }
// },

// CHAPTER {
// @Override
// public String getSystemPrompt() {
// return "You are a helpful assistant. Create a comprehensive chapter summary
// from these page summaries.";
// }

// @Override
// public AbstractSummaryModel createSummary(String content) {
// return new ChapterSummaryModel(content);
// }
// },

// BOOK {
// @Override
// public String getSystemPrompt() {
// return "You are a helpful assistant. Create an overall book summary from
// these chapter summaries.";
// }

// @Override
// public AbstractSummaryModel createSummary(String content) {
// return new BookSummaryModel(content);
// }
// }
// }
