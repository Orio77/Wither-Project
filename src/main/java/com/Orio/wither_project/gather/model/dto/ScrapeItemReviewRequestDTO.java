package com.Orio.wither_project.gather.model.dto;

import java.util.List;

import com.Orio.wither_project.gather.model.ScrapeResult.ScrapeItem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScrapeItemReviewRequestDTO {
    private String reviewId;
    private List<ScrapeItem> items;
}
