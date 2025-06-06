package com.Orio.wither_project.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class DataBaseConfig {
    @Value("${database.sql.password}")
    public String sqlPassword;

    @Value("${database.vectorstore.password}")
    public String vsPassword;
}
