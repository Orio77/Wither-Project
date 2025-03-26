package com.Orio.wither_project.process.summary.service.storage;

import java.util.List;

import com.Orio.wither_project.process.summary.model.ChapterModel;
import com.Orio.wither_project.process.summary.model.DocumentModel;
import com.Orio.wither_project.process.summary.model.PageModel;

public interface ISQLDocumentService {
    boolean savePage(PageModel page);

    boolean savePages(List<PageModel> pages);

    List<PageModel> getPages(String chapterTitle);

    List<PageModel> getUnprocessedPagesByChapter(String chapterTitle);

    boolean saveChapters(List<ChapterModel> chapters);

    List<ChapterModel> getChapters(String documentTitle);

    List<ChapterModel> getUnprocessedChaptersByDocument(String documentTitle);

    boolean saveDoc(DocumentModel documentModel);

    DocumentModel getDocument(String title);

    List<DocumentModel> getAllDocs();

    void deleteDoc(String title);
}
