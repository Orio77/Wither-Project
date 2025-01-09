// package com.Orio.wither_project.service.summary;

// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.junit.jupiter.api.Assertions.assertTrue;

// import java.util.Arrays;
// import java.util.List;

// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;

// import com.Orio.wither_project.WitherProject;
// import com.Orio.wither_project.model.AbstractSummaryModel;
// import com.Orio.wither_project.model.TestSummaryModel;

// @SpringBootTest(classes = WitherProject.class)
// class BookSummaryServiceTest {

// @Autowired
// private IBookSummaryService summaryService;

// private static class TestProgressiveSummaryModel extends TestSummaryModel
// implements ProgressivelySummarizable<TestSummaryModel> {

// @Override
// public String getText(List<TestSummaryModel> parts) {
// // Implement the logic to concatenate the text from parts
// return parts.stream()
// .map(TestSummaryModel::getContent)
// .reduce("", (a, b) -> a + " " + b);
// }

// @Override
// public List<TestSummaryModel> split() {
// // Implement the logic to split the content into smaller parts
// // For simplicity, let's assume we split by sentences
// String[] parts = getContent().split("\\. ");
// return Arrays.stream(parts)
// .map(TestSummaryModel::new)
// .toList();
// }

// @Override
// public void setContent(String content) {
// super.setContent(content);
// }

// @Override
// public String getContent() {
// return super.getContent();
// }

// public TestProgressiveSummaryModel(String content) {
// super(content);
// }
// }

// private static enum TestSummaryType implements ISummaryType {
// TEST {
// @Override
// public String getSystemPrompt() {
// return "You are a helpful assistant. Summarize the following text.";
// }

// @Override
// public AbstractSummaryModel createSummary(String content) {
// return new TestSummaryModel(content);
// }
// },

// PROGRESSIVE_TEST {
// @Override
// public String getSystemPrompt() {
// return "You are a helpful assistant. Summarize the following text.";
// }

// @Override
// public AbstractSummaryModel createSummary(String content) {
// return new TestProgressiveSummaryModel(content);
// }
// }
// }

// @BeforeEach
// void setUp() {

// }

// @AfterEach
// void tearDown() {
// // No cleanup needed as we're not persisting any data
// }

// @Test
// void testCreateBasicSummary() {
// String content = "This is a test content that needs to be summarized. " +
// "It contains multiple sentences with various information. " +
// "The summary should be shorter than the original text.";

// TestSummaryModel summary = summaryService.createSummary(content,
// TestSummaryType.TEST);

// assertNotNull(summary);
// assertNotNull(summary.getContent());
// assertFalse(summary.getContent().isEmpty());
// // Summary should be shorter than original content
// assertTrue(summary.getContent().length() < content.length());
// }

// @Test
// void testCreateProgressiveSummary() {
// List<TestSummaryModel> inputs = Arrays.asList(
// new TestSummaryModel("First part of the content to be summarized."),
// new TestSummaryModel("Second part with different information."),
// new TestSummaryModel("Third part adding more context to be combined."));

// TestProgressiveSummaryModel container = new TestProgressiveSummaryModel("");
// TestProgressiveSummaryModel summary = (TestProgressiveSummaryModel)
// summaryService.createProgressiveSummary(
// inputs, TestSummaryType.PROGRESSIVE_TEST, container);

// assertNotNull(summary);
// assertNotNull(summary.getContent());
// assertFalse(summary.getContent().isEmpty());
// }

// @Test
// void testNullInputs() {
// assertThrows(NullPointerException.class, () ->
// summaryService.createSummary(null, TestSummaryType.TEST));

// assertThrows(NullPointerException.class, () ->
// summaryService.createSummary("content", null));

// assertThrows(NullPointerException.class,
// () -> summaryService.createProgressiveSummary(null,
// TestSummaryType.PROGRESSIVE_TEST,
// new TestProgressiveSummaryModel("")));

// assertThrows(NullPointerException.class,
// () -> summaryService.createProgressiveSummary(List.of(), null, new
// TestProgressiveSummaryModel("")));
// }
// }
