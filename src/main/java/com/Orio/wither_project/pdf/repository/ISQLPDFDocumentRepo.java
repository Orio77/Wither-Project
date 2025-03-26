package com.Orio.wither_project.pdf.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Orio.wither_project.process.summary.model.DocumentModel;

@Repository
public interface ISQLPDFDocumentRepo extends JpaRepository<DocumentModel, Long> {
    Optional<DocumentModel> findByTitle(String title);

    List<DocumentModel> findByTitleContainingIgnoreCase(String titlePart);
}