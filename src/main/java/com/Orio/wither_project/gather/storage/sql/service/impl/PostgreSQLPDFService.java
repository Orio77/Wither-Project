package com.Orio.wither_project.gather.storage.sql.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.Orio.wither_project.gather.storage.sql.service.ISQLPDFService;
import com.Orio.wither_project.pdf.model.DocumentModel;
import com.Orio.wither_project.repository.ISQLPDFDocumentRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostgreSQLPDFService implements ISQLPDFService {

    private final ISQLPDFDocumentRepo pdfDocumentRepo;

    @Override
    public boolean saveDocument(DocumentModel document) {
        DocumentModel savedDocument = pdfDocumentRepo.save(document);
        return savedDocument != null;
    }

    @Override
    public Optional<DocumentModel> getDocumentById(Long id) {
        return pdfDocumentRepo.findById(id);
    }

    @Override
    public Optional<DocumentModel> getDocumentByName(String fileName) {
        return pdfDocumentRepo.findByTitle(fileName);
    }

    @Override
    public List<DocumentModel> searchDocumentsByName(String fileNamePart) {
        return pdfDocumentRepo.findByTitleContainingIgnoreCase(fileNamePart);
    }

    @Override
    public List<DocumentModel> getAllDocuments() {
        return pdfDocumentRepo.findAll();
    }

    @Override
    public void deleteDocument(Long id) {
        pdfDocumentRepo.deleteById(id);
    }
}
