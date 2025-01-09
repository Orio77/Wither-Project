package com.Orio.wither_project.gather.storage.sql.service;

import java.util.List;
import java.util.Optional;

import com.Orio.wither_project.pdf.model.DocumentModel;

public interface ISQLPDFService {

    public boolean saveDocument(DocumentModel document);

    public Optional<DocumentModel> getDocumentById(Long id);

    public Optional<DocumentModel> getDocumentByName(String fileName);

    public List<DocumentModel> searchDocumentsByName(String fileNamePart);

    public List<DocumentModel> getAllDocuments();

    public void deleteDocument(Long id);
}
