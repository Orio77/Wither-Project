package com.Orio.wither_project.summary.service.extraction;

import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;

import com.Orio.wither_project.summary.model.ChapterModel;
import com.Orio.wither_project.summary.model.PageModel;

public interface IPDFContentExtractionService {

    List<PageModel> getPages(PDDocument doc);

    List<ChapterModel> getChapters(PDDocument doc);
}
