package com.Orio.wither_project.gader.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DataSource {
    private String query;
    @Builder.Default
    private List<SearchResult.SearchItem> items = new ArrayList<>();
    @Builder.Default
    private List<Exception> errors = new ArrayList<>();

    public void addError(Exception error) {
        errors.add(error);
    }

    public void addSearchItem(SearchResult.SearchItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
    }

}
