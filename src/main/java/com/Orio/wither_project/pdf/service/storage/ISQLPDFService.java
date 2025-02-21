package com.Orio.wither_project.pdf.service.storage;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.Orio.wither_project.pdf.model.entity.FileEntity;

public interface ISQLPDFService {

    public boolean savePDF(MultipartFile pdf);

    public FileEntity getPDF(String name);

    public List<FileEntity> getAllPDFs();
}
