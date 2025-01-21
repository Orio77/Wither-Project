package com.Orio.wither_project.pdf.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Orio.wither_project.pdf.model.DocumentModel;

import java.util.Optional;

@Repository
public interface PDFRepo extends JpaRepository<DocumentModel, Long> {
    Optional<DocumentModel> findByTitle(String title);

    Optional<DocumentModel> findByFileName(String fileName);
}
