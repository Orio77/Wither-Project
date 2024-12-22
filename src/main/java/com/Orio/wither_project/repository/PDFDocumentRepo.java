package com.Orio.wither_project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Orio.wither_project.model.BookModel;

@Repository
public interface PDFDocumentRepo extends JpaRepository<BookModel, Long> {
    Optional<BookModel> findByTitle(String title);

    List<BookModel> findByTitleContainingIgnoreCase(String titlePart);
}