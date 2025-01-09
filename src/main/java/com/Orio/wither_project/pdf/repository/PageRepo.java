package com.Orio.wither_project.pdf.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Orio.wither_project.pdf.model.PageModel;

import java.util.List;

@Repository
public interface PageRepo extends JpaRepository<PageModel, Long> {
    List<PageModel> findByChapterTitleOrderByPageNumber(String chapterTitle);
}
