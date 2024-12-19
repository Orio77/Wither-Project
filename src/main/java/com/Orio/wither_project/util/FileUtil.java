package com.Orio.wither_project.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Orio.wither_project.model.DataModel;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DATA_DIR = "data";
    private static final String MAP_CONTENT_TYPE = "URL content map";
    private static final String RESPONSE_CONTENT_TYPE = "response";

    private FileUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static void saveResponseAsJson(Object response, String fileName) throws IOException {
        saveToJson(response, fileName, RESPONSE_CONTENT_TYPE);
    }

    public static void saveUrlContentAsJson(List<DataModel> dataList, String fileName) throws IOException {
        saveToJson(dataList, fileName, MAP_CONTENT_TYPE);
    }

    private static void saveToJson(Object content, String fileName, String contentType) throws IOException {
        Objects.requireNonNull(content, "Content cannot be null");
        Objects.requireNonNull(fileName, "File name cannot be null");

        Path filePath = Paths.get(fileName);
        logger.debug("Saving {} to file: {}", contentType, fileName);

        try {
            objectMapper.writeValue(filePath.toFile(), content);
            logger.debug("Successfully saved {} to file: {}", contentType, fileName);
        } catch (IOException e) {
            logger.error("Error saving {} to file: {}", contentType, fileName, e);
            throw e;
        }
    }

    public static void createNewDataDir(String query) throws IOException {
        Objects.requireNonNull(query, "Query cannot be null");

        Path dirPath = Paths.get(DATA_DIR, query.trim());
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
            logger.debug("Created new directory: {}", dirPath);
        }
    }
}