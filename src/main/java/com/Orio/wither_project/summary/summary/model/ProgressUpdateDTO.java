package com.Orio.wither_project.summary.summary.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProgressUpdateDTO {
    private int currentPage;
    private int totalPages;
}
