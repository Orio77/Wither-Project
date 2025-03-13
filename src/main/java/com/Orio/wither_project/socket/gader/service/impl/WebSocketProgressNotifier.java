package com.Orio.wither_project.socket.gader.service.impl;

import org.springframework.stereotype.Service;

import com.Orio.wither_project.gader.model.QAModel;
import com.Orio.wither_project.socket.gader.service.IProcessingProgressNotifier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketProgressNotifier implements IProcessingProgressNotifier {

    private final QAProgressService qaProgressService;

    @Override
    public void notifyQAResult(QAModel qaModel) {
        qaProgressService.sendQAIdeasUpdate(qaModel);
    }

    @Override
    public void notifyProgress(int processed, int total) {
        qaProgressService.sendQAProgressUpdate(processed, total);
    }
}