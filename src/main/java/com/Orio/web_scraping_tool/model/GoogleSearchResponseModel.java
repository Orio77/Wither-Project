package com.Orio.web_scraping_tool.model;

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
