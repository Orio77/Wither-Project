package com.Orio.wither_project.gather.model;

import java.util.ArrayList;
import java.util.List;

import com.Orio.wither_project.gather.model.ScrapeResult.ScrapeItem;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataModel {
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
}
