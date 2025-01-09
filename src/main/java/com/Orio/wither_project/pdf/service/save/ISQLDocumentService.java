package com.Orio.wither_project.pdf.service.save;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.Orio.wither_project.pdf.model.ChapterModel;
import com.Orio.wither_project.pdf.model.DocumentModel;
import com.Orio.wither_project.pdf.model.PageModel;
import com.Orio.wither_project.pdf.repository.entity.FileEntity;

public interface ISQLDocumentService {

    public boolean savePDF(MultipartFile pdf);

    public FileEntity getPDF(String name);

    public boolean savePages(List<PageModel> pages);

    public List<PageModel> getPages(String chapterTitle);

    public boolean saveChapters(List<ChapterModel> chapters);

    public List<ChapterModel> getChapters(String documentTitle);

    public boolean saveDoc(DocumentModel documentModel);

    public DocumentModel getDocument(String title);

}
