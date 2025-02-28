package com.Orio.wither_project.summary.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Orio.wither_project.summary.model.ChapterModel;

@Repository
public interface ChapterRepo extends JpaRepository<ChapterModel, Long> { // Need to save DocumentModel before

    @Query("SELECT c FROM ChapterModel c WHERE c.doc.title = :documentTitle")
    List<ChapterModel> findByDocumentTitle(@Param("documentTitle") String documentTitle);

    @Query("SELECT c FROM ChapterModel c WHERE c.doc.title = :documentTitle AND c.summary IS NULL ORDER BY c.chapterNumber")
    List<ChapterModel> findUnsummarizedChapters(@Param("documentTitle") String documentTitle);
}
