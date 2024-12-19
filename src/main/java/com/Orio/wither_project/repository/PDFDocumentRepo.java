package com.Orio.wither_project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Orio.wither_project.model.PDFDocument;

@Repository
public interface PDFDocumentRepo extends JpaRepository<PDFDocument, Long> {

    List<PDFDocument> findByFileName(String fileName);

    List<PDFDocument> findByFileNameContainingIgnoreCase(String fileNamePart);
}
