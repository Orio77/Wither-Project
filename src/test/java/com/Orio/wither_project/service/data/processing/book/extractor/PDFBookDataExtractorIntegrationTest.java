// package com.Orio.wither_project.service.data.processing.book.extractor;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertNotNull;

// import java.io.File;
// import java.io.IOException;
// import java.nio.charset.StandardCharsets;
// import java.util.List;

// import org.apache.pdfbox.Loader;
// import org.apache.pdfbox.pdmodel.PDDocument;
// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;

// import com.Orio.wither_project.model.ChapterModel;
// import com.Orio.wither_project.model.PageModel;

// @SpringBootTest
// public class PDFBookDataExtractorIntegrationTest {

// @Autowired
// private PDFBookDataExtractor pdfExtractor;

// private PDDocument testDoc;

// @BeforeEach
// void setUp() throws IOException {
// ClassLoader classLoader = getClass().getClassLoader();
// File file = new File(classLoader.getResource("test.pdf").getFile());
// String decodedPath = java.net.URLDecoder.decode(file.getAbsolutePath(),
// StandardCharsets.UTF_8.name());
// testDoc = Loader.loadPDF(new File(decodedPath));
// }

// @AfterEach
// void tearDown() throws IOException {
// if (testDoc != null) {
// testDoc.close();
// }
// }

// @Test
// void getChapters_ShouldReturnCorrectChapters() throws IOException {
// // Act
// List<ChapterModel> chapters = pdfExtractor.getChapters(testDoc, null);

// // Assert
// assertNotNull(chapters);
// assertEquals(2, chapters.size());
// assertNotNull(chapters.get(0).getPages().get(0).getContent());
// assertNotNull(chapters.get(1).getPages().get(0).getContent());
// }

// @Test
// void getPages_ShouldReturnAllPages() throws IOException {
// // Act
// List<PageModel> pages = pdfExtractor.getPages(testDoc);

// // Assert
// assertNotNull(pages);
// assertNotNull(pages.get(0).getContent());
// assertNotNull(pages.get(1).getContent());
// }

// @Test
// void extractAuthor_ShouldReturnCorrectAuthor() {
// // Act
// String author = pdfExtractor.extractAuthor(testDoc);

// // Assert
// assertNotNull(author);
// }

// @Test
// void extractTitle_ShouldReturnCorrectTitle() {
// // Act
// String title = pdfExtractor.extractTitle(testDoc);

// // Assert
// assertNotNull(title);
// }
// }
