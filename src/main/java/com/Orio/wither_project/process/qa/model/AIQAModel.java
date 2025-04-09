package com.Orio.wither_project.process.qa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIQAModel {
    private String first_three_words;
    private String last_three_words;
    private String question;
}
