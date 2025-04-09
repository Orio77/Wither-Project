package com.Orio.wither_project.process.qa.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SplitConfigModel {

    private int contentPartMaxSize;
    private int contentOverlapCharacters;
}
