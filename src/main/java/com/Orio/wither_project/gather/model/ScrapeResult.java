package com.Orio.wither_project.gather.model;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class ScrapeResult {
    private String query;
    @Builder.Default
    private List<ScrapeItem> items = new ArrayList<>();
    @Builder.Default
    private List<Exception> errors = new ArrayList<>();

    public void addError(Exception error) {
        errors.add(error);
    }

    public void addItem(ScrapeItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScrapeItem {
        private String title;
        private String link;
        private String content;
        private String description;
        private String author;
        private String publishDate;
        private Exception error;

    }
}
