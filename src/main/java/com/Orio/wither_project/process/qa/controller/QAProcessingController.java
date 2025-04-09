package com.Orio.wither_project.process.qa.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Orio.wither_project.core.constants.ApiPaths;
import com.Orio.wither_project.process.qa.model.dto.QAProcessRequestDTO;
import com.Orio.wither_project.process.qa.service.orchestration.IQAFrameworkOrchestrationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.BASE)
public class QAProcessingController {

    private final IQAFrameworkOrchestrationService orchestrationService;

    @PostMapping(ApiPaths.QA)
    public ResponseEntity<Void> process(@RequestBody QAProcessRequestDTO request) {

        orchestrationService.processAndSave(request);

        return ResponseEntity.ok(null);
    }

}
