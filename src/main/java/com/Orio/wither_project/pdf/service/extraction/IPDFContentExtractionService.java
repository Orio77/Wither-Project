package com.Orio.wither_project.pdf.service.extraction;

import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;

import com.Orio.wither_project.pdf.model.ChapterModel;
import com.Orio.wither_project.pdf.model.PageModel;

public interface IPDFContentExtractionService {

    List<PageModel> getPages(PDDocument doc);

    List<ChapterModel> getChapters(PDDocument doc);
}
