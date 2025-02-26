package com.Orio.wither_project.summary.repository;

import com.Orio.wither_project.summary.model.ProcessingProgressModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProcessingProgressRepository extends JpaRepository<ProcessingProgressModel, Long> {
    Optional<ProcessingProgressModel> findByFileName(String fileName);
}