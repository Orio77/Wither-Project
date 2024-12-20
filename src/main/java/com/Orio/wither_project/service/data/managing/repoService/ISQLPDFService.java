package com.Orio.wither_project.service.data.managing.repoService;

import java.util.List;
import java.util.Optional;

import com.Orio.wither_project.model.PDFDocument;

public interface ISQLPDFService {

    public boolean saveDocument(PDFDocument document);

    public Optional<PDFDocument> getDocumentById(Long id);

    public Optional<PDFDocument> getDocumentByName(String fileName);

    public List<PDFDocument> searchDocumentsByName(String fileNamePart);

    public List<PDFDocument> getAllDocuments();

    public void deleteDocument(Long id);
}
