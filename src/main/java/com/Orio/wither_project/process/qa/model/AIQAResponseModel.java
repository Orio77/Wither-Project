package com.Orio.wither_project.process.qa.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIQAResponseModel {
    private List<AIQAModel> aiqaModels;
}
