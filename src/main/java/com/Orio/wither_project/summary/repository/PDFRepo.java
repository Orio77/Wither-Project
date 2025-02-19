package com.Orio.wither_project.summary.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Orio.wither_project.summary.model.DocumentModel;

import jakarta.transaction.Transactional;
import java.util.Optional;

@Repository
public interface PDFRepo extends JpaRepository<DocumentModel, Long> {
    Logger logger = LoggerFactory.getLogger(PDFRepo.class);

    Optional<DocumentModel> findByTitle(String title);

    Optional<DocumentModel> findByFileName(String fileName);

    @Transactional
    default boolean deleteByTitleOrFileName(String title) {
        logger.debug("Attempting to delete document with title/filename: {}", title);

        Optional<DocumentModel> documentByTitle = findByTitle(title);
        if (documentByTitle.isPresent()) {
            logger.info("Deleting document found by title: {}", title);
            delete(documentByTitle.get());
            return true;
        } else {
            logger.warn("No document found to delete with title: {}", title);
        }

        Optional<DocumentModel> documentByFileName = findByFileName(title);
        if (documentByFileName.isPresent()) {
            logger.info("Deleting document found by filename: {}", title);
            delete(documentByFileName.get());
            return true;
        } else {
            logger.warn("No document found to delete with filename: {}", title);
        }

        return false;
    }
}