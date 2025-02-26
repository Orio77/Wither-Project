package com.Orio.wither_project.summary.service.orchestration;

import java.io.IOException;

import com.Orio.wither_project.pdf.model.entity.FileEntity;
import com.Orio.wither_project.summary.model.DocumentModel;

public interface IPDFProcessingOrchestrationService {
    default boolean processPDF(FileEntity file) throws IOException {
        DocumentModel doc = convert(file);
        if (doc == null)
            return false;

        save(doc);

        return setMetadata(doc) &&
                setContents(doc) &&
                setSummaries(doc);
    }

    boolean continueProcessingPDF(FileEntity file) throws IOException;

    DocumentModel convert(FileEntity file) throws IOException;

    boolean setMetadata(DocumentModel model);

    boolean setContents(DocumentModel model);

    boolean setSummaries(DocumentModel model);

    boolean setPageSummaries(DocumentModel model);

    boolean setChapterSummaries(DocumentModel model);

    boolean setDocumentSummary(DocumentModel model);

    void save(DocumentModel model);
}