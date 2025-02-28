package com.Orio.wither_project.pdf.service.storage;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.Orio.wither_project.pdf.model.entity.FileEntity;

public interface ISQLPDFService {

    boolean savePDF(MultipartFile pdf, String name);

    FileEntity getPDFByName(String name);

    FileEntity getPDFByFileName(String fileName);

    List<FileEntity> getAllPDFs();
}
