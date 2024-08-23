package com.Orio.web_scraping_tool.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class OllamaResponseModel {
    private final String fragment;
    private final String question;
    private final String answer;

    @JsonCreator
    public OllamaResponseModel(
            @JsonProperty("fragment") String fragment,
            @JsonProperty("question") String question,
            @JsonProperty("answer") String answer) {
        this.fragment = fragment;
        this.question = question;
        this.answer = answer;
    }
}
