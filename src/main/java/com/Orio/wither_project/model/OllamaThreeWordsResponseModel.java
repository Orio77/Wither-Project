package com.Orio.wither_project.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OllamaThreeWordsResponseModel {
    private final String first_three_words;
    private final String last_three_words;
    private final String question;

    @JsonCreator
    public OllamaThreeWordsResponseModel(
            @JsonProperty("first_three_words") String first_three_words,
            @JsonProperty("last_three_words") String last_three_words,
            @JsonProperty("question") String question) {
        this.first_three_words = first_three_words;
        this.last_three_words = last_three_words;
        this.question = question;
    }
}