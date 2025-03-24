package com.Orio.wither_project.gather.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Orio.wither_project.constants.ApiPaths;
import com.Orio.wither_project.gather.model.dto.ScrapeItemReviewResultDTO;
import com.Orio.wither_project.socket.gather.service.impl.ScrapeItemReviewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(ApiPaths.BASE)
@RequiredArgsConstructor
@Slf4j
public class ScrapeItemReviewController {

    private final ScrapeItemReviewService dataModelReviewService;

    @PostMapping(ApiPaths.REVIEW_COMPLETE)
    public ResponseEntity<Void> completeReview(@RequestBody ScrapeItemReviewResultDTO result) {
        log.info("Received review completion for ID: {}", result.getReviewId());
        dataModelReviewService.completeReview(result);
        return ResponseEntity.ok().build();
    }
}