package com.Orio.wither_project.process.summary.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class SummaryConstantsConfig {

    @Value("${wither.summary.metadata.pdf.filename}")
    private String fileName;

    @Value("${wither.summary.constants.regex.split}")
    private String splitRegex;
}
