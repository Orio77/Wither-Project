package com.Orio.wither_project.summary.service.storage;

import java.util.List;

import com.Orio.wither_project.summary.model.ChapterModel;
import com.Orio.wither_project.summary.model.DocumentModel;
import com.Orio.wither_project.summary.model.PageModel;

public interface ISQLDocumentService {

    public boolean savePages(List<PageModel> pages);

    public List<PageModel> getPages(String chapterTitle);

    public boolean saveChapters(List<ChapterModel> chapters);

    public List<ChapterModel> getChapters(String documentTitle);

    public boolean saveDoc(DocumentModel documentModel);

    public DocumentModel getDocument(String title);

    void deleteDoc(String title);

    List<DocumentModel> getAllDocs();

}
