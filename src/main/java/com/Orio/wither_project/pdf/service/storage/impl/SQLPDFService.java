package com.Orio.wither_project.pdf.service.storage.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.Orio.wither_project.pdf.model.entity.FileEntity;
import com.Orio.wither_project.pdf.repository.PDFRepo;
import com.Orio.wither_project.pdf.service.storage.ISQLPDFService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SQLPDFService implements ISQLPDFService {

    private static final Logger logger = LoggerFactory.getLogger(SQLPDFService.class);

    private final PDFRepo fileRepo;

    @Override
    public boolean savePDF(MultipartFile pdf, String name) {
        try {
            FileEntity fileEntity = new FileEntity();
            fileEntity.setFileName(pdf.getOriginalFilename());
            fileEntity.setName(name);
            fileEntity.setContentType(pdf.getContentType());
            fileEntity.setData(pdf.getBytes());
            return fileRepo.saveAndFlush(fileEntity) != null;
        } catch (IOException e) {
            logger.error("Error saving PDF file", e);
            return false;
        }
    }

    @Override
    public FileEntity getPDFByName(String name) {
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

    @Override
    public FileEntity getPDFByFileName(String fileName) {
        logger.info("Retrieving PDF file by fileName: {}", fileName);
        try {
            FileEntity fileEntity = fileRepo.findByFileName(fileName);
            if (fileEntity == null) {
                logger.warn("PDF file not found with fileName: {}", fileName);
                return null;
            }
            return fileEntity;
        } catch (Exception e) {
            logger.error("Error retrieving PDF file with fileName: {}", fileName, e);
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
