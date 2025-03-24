package com.Orio.wither_project.gather.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.Orio.wither_project.gather.model.ScrapeResult.ScrapeItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataModel {
    @Builder.Default
    private String id = UUID.randomUUID().toString();
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

    // Initialize ID if not provided
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }
}
