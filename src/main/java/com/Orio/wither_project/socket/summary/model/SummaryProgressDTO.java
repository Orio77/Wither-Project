package com.Orio.wither_project.socket.summary.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SummaryProgressDTO {
    private int currentPage;
    private int totalPages;
}
