package com.Orio.wither_project.gader.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchResult {
    private String query;
    @Builder.Default
    private List<Item> items = new ArrayList<>();
    @Builder.Default
    private List<Exception> errors = new ArrayList<>();

    public void addError(Exception error) {
        errors.add(error);
    }

    public void addItem(Item item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
    }

    @Data
    @Builder
    public static class Item {
        private String title;
        private String link;
    }
}
