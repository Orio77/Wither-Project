package com.Orio.wither_project.summary.service.extraction;

import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;

import com.Orio.wither_project.summary.model.ChapterModel;

public interface IPDFChapterExtractionService {

    List<ChapterModel> extract(PDDocument doc);
}
