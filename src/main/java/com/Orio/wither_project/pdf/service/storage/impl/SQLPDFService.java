package com.Orio.wither_project.pdf.service.storage.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.Orio.wither_project.pdf.repository.FilePDFRepo;
import com.Orio.wither_project.pdf.repository.entity.FileEntity;
import com.Orio.wither_project.pdf.service.storage.ISQLPDFService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SQLPDFService implements ISQLPDFService {

    private static final Logger logger = LoggerFactory.getLogger(SQLPDFService.class);

    private final FilePDFRepo fileRepo;

    @Override
    public boolean savePDF(MultipartFile pdf) {
        try {
            FileEntity fileEntity = new FileEntity();
            fileEntity.setName(pdf.getOriginalFilename());
            fileEntity.setContentType(pdf.getContentType());
            fileEntity.setData(pdf.getBytes());
            return fileRepo.saveAndFlush(fileEntity) != null;
        } catch (IOException e) {
            logger.error("Error saving PDF file", e);
            return false;
        }
    }

    @Override
    public FileEntity getPDF(String name) {
        logger.info("Retrieving PDF file: {}", name);
        try {
            FileEntity fileEntity = fileRepo.findByName(name);
            if (fileEntity == null) {
                logger.warn("PDF file not found: {}", name);
                return null;
            }
            return fileEntity;
        } catch (Exception e) {
            logger.error("Error retrieving PDF file: {}", name, e);
            return null;
        }
    }

    public List<FileEntity> getAllPDFs() {
        logger.info("Retrieving all PDF files");
        try {
            return fileRepo.findAll();
        } catch (Exception e) {
            logger.error("Error retrieving all PDF files", e);
            return Collections.emptyList();
        }
    }
}
