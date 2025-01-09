package com.Orio.wither_project.pdf.service.conversion;

import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import com.Orio.wither_project.pdf.model.DocumentModel;
import com.Orio.wither_project.pdf.repository.entity.FileEntity;

public interface IPDFConversionService {

    default DocumentModel convert(FileEntity file) throws IOException {
        PDDocument pddoc = convertToPdDocument(file);
        DocumentModel model = convertToDocumentModel(pddoc);
        return model;
    }

    public DocumentModel convertToDocumentModel(PDDocument pddoc);

    default PDDocument convertToPdDocument(FileEntity file) throws IOException {
        return Loader.loadPDF(file.getData());
    }
}
