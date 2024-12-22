package com.Orio.wither_project.service.data.managing.repoService;

import java.util.List;
import java.util.Optional;

import com.Orio.wither_project.model.BookModel;

public interface ISQLPDFService {

    public boolean saveDocument(BookModel document);

    public Optional<BookModel> getDocumentById(Long id);

    public Optional<BookModel> getDocumentByName(String fileName);

    public List<BookModel> searchDocumentsByName(String fileNamePart);

    public List<BookModel> getAllDocuments();

    public void deleteDocument(Long id);
}
