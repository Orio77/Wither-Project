package com.Orio.wither_project.process.qa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class QAProcessingConfig {

    @Value("${wither.gather.processing.content.parts}")
    private int contentParts;

    @Value("${wither.gather.processing.content.part.max.size}")
    private int contentPartMaxSize;

    @Value("${wither.gather.processing.content.overlap.characters}")
    private int contentOverlapCharacters;

    @Value("${wither.gather.processing.thread.pool.size}")
    private int threadPoolSize;

}
