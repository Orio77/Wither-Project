package com.Orio.wither_project.process.summary.service.orchestration;

import java.io.IOException;

import com.Orio.wither_project.pdf.model.entity.FileEntity;
import com.Orio.wither_project.process.summary.model.DocumentModel;

public interface IPDFProcessingOrchestrationService {
    default boolean processPDF(FileEntity file) throws IOException {
        if (file == null || file.getFileName() == null || file.getFileName().trim().isEmpty()) {
            return false;
        }

        DocumentModel doc = convert(file);
        if (doc == null)
            return false;

        boolean metadataSet = setMetadata(doc);
        boolean contentsSet = setContents(doc);
        save(doc);
        boolean summariesSet = setSummaries(doc);

        return metadataSet && contentsSet && summariesSet;
    }

    DocumentModel convert(FileEntity file) throws IOException;

    boolean setMetadata(DocumentModel model);

    boolean setContents(DocumentModel model);

    boolean setSummaries(DocumentModel model);

    void save(DocumentModel model);
}
