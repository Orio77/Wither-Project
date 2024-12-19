package com.Orio.wither_project.model;

import java.util.List;

import lombok.Data;

@Data
public class GoogleSearchResponseModel {
    List<Item> items;

    @Data
    public static class Item {
        private String title;
        private String link;
    }
}
