package com.Orio.wither_project.pdf.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Orio.wither_project.pdf.model.entity.FileEntity;

import jakarta.transaction.Transactional;

@Repository
public interface PDFRepo extends JpaRepository<FileEntity, Long> {
    FileEntity findByName(String name);

    @Transactional
    long deleteByName(String name);
}
