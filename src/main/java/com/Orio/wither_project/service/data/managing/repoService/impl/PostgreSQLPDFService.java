package com.Orio.wither_project.service.data.managing.repoService.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.Orio.wither_project.model.PDFDocument;
import com.Orio.wither_project.repository.PDFDocumentRepo;
import com.Orio.wither_project.service.data.managing.repoService.ISQLPDFService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostgreSQLPDFService implements ISQLPDFService {

    private final PDFDocumentRepo pdfDocumentRepo;

    @Override
    public boolean saveDocument(PDFDocument document) {
        PDFDocument savedDocument = pdfDocumentRepo.save(document);
        return savedDocument != null;
    }

    @Override
    public Optional<PDFDocument> getDocumentById(Long id) {
        return pdfDocumentRepo.findById(id);
    }

    @Override
    public Optional<PDFDocument> getDocumentByName(String fileName) {
        return pdfDocumentRepo.findByFileName(fileName);
    }

    @Override
    public List<PDFDocument> searchDocumentsByName(String fileNamePart) {
        return pdfDocumentRepo.findByFileNameContainingIgnoreCase(fileNamePart);
    }

    @Override
    public List<PDFDocument> getAllDocuments() {
        return pdfDocumentRepo.findAll();
    }

    @Override
    public void deleteDocument(Long id) {
        pdfDocumentRepo.deleteById(id);
    }
}
