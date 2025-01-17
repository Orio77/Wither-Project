package com.Orio.wither_project.pdf.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Orio.wither_project.pdf.repository.entity.FileEntity;

@Repository
public interface FileRepo extends JpaRepository<FileEntity, Long> {
    FileEntity findByName(String name);
}
