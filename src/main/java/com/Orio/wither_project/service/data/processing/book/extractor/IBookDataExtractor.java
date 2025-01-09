package com.Orio.wither_project.service.data.processing.book.extractor;

import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;

import com.Orio.wither_project.pdf.model.ChapterModel;
import com.Orio.wither_project.pdf.model.PageModel;

public interface IBookDataExtractor {
    List<ChapterModel> getChapters(PDDocument document, List<Integer> startPages) throws IOException;

    List<PageModel> getPages(PDDocument document) throws IOException;

    String extractAuthor(PDDocument document);

    String extractTitle(PDDocument document);
}
