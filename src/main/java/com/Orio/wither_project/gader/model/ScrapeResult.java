package com.Orio.wither_project.gader.model;

import java.util.ArrayList;
import java.util.List;

import com.Orio.wither_project.gader.model.SearchResult.Item;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScrapeResult {
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
}
