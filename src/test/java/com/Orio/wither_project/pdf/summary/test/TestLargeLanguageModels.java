package com.Orio.wither_project.pdf.summary.test;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "custom.ai.model")
@Getter
@Setter
public class TestLargeLanguageModels {

    private List<String> models;

    public List<String> getAllModels() {
        return models;
    }
}
