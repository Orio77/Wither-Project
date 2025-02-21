package com.Orio.wither_project.summary.service.conversion;

import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import com.Orio.wither_project.pdf.model.entity.FileEntity;
import com.Orio.wither_project.summary.model.DocumentModel;

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
