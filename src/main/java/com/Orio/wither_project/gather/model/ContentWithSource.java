package com.Orio.wither_project.gather.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentWithSource {
    private String content;
    private String source;
}
