// package com.Orio.wither_project.service.data.processing.book.impl;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertNotNull;

// import java.io.File;
// import java.io.IOException;
// import java.nio.charset.StandardCharsets;
// import java.util.List;

// import org.apache.pdfbox.Loader;
// import org.apache.pdfbox.pdmodel.PDDocument;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;

// import com.Orio.wither_project.model.BookModel;
// import com.Orio.wither_project.model.ChapterModel;
// import com.Orio.wither_project.model.PageModel;
// import com.Orio.wither_project.model.PageSummaryModel;
// import
// com.Orio.wither_project.service.data.processing.book.IAIBookProcessingService;

// @SpringBootTest
// class OllamaBookProcessingServiceTest {

// @Autowired
// private IAIBookProcessingService service;
// private PDDocument testDoc;

// @BeforeEach
// void setUp() throws IOException {
// ClassLoader classLoader = getClass().getClassLoader();
// File file = new File(classLoader.getResource("test.pdf").getFile());
// String decodedPath = java.net.URLDecoder.decode(file.getAbsolutePath(),
// StandardCharsets.UTF_8.name());
// testDoc = Loader.loadPDF(new File(decodedPath));
// }

// @Test
// void processPDFDocument_ShouldReturnCompleteBookModel() throws IOException {
// // Act
// BookModel result = service.processPDFDocument(testDoc, "test.pdf", null);

// // Assert
// assertNotNull(result);
// assertEquals("test.pdf", result.getTitle());
// assertFalse(result.getChapters().isEmpty());
// assertNotNull(result.getSummary());
// assertFalse(result.getSummary().getContent().isEmpty());
// }

// @Test
// void getChapters_ShouldReturnChaptersList() throws IOException {
// // Act
// List<ChapterModel> result = service.getChapters(testDoc, null);

// // Assert
// assertNotNull(result);
// assertFalse(result.isEmpty());
// assertNotNull(result.get(0).getTitle());
// assertNotNull(result.get(0).getContent());
// }

// @Test
// void getPageSummaries_ShouldReturnPageSummariesList() throws IOException {
// List<PageModel> pages = service.getPages(testDoc);

// // Act
// List<PageSummaryModel> result = service.getPageSummaries(pages);

// // Assert
// assertNotNull(result);
// assertFalse(result.isEmpty());
// assertNotNull(result.get(0).getContent());
// assertFalse(result.get(0).getContent().isEmpty());
// }
// }
