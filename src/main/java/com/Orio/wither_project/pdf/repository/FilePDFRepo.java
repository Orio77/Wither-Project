package com.Orio.wither_project.pdf.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Orio.wither_project.pdf.repository.entity.FileEntity;

import jakarta.transaction.Transactional;

@Repository
public interface FilePDFRepo extends JpaRepository<FileEntity, Long> { // TODO Rename to PDFRepo
    FileEntity findByName(String name);

    @Transactional
    long deleteByName(String name);
}
