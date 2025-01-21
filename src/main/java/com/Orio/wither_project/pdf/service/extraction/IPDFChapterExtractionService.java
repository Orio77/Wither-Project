package com.Orio.wither_project.pdf.service.extraction;

import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;

import com.Orio.wither_project.pdf.model.ChapterModel;

public interface IPDFChapterExtractionService {

    List<ChapterModel> extract(PDDocument doc);
}
