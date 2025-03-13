package com.Orio.wither_project.socket.gader.service;

import com.Orio.wither_project.gader.model.QAModel;

/**
 * Interface for notifying of processing progress
 */
public interface IProcessingProgressNotifier {

    /**
     * Notify about a new QA result
     * 
     * @param qaModel The QA model that was processed
     */
    void notifyQAResult(QAModel qaModel);

    /**
     * Notify about overall progress
     * 
     * @param processed Number of items processed
     * @param total     Total items to process
     */
    void notifyProgress(int processed, int total);
}