package com.Orio.wither_project.pdf.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Orio.wither_project.pdf.model.ChapterModel;

import java.util.List;

@Repository
public interface ChapterRepo extends JpaRepository<ChapterModel, Long> {
    List<ChapterModel> findByDocTitle(String title);
}
