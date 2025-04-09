package com.Orio.wither_project.socket.process.service;

import com.Orio.wither_project.process.qa.model.QAModel;

public interface IQAProgressNotifier {

    void notifyQAResult(QAModel qaModel);

    void notifyProgress(int processed, int total);
}
