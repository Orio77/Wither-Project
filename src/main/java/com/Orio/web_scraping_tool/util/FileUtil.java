package com.Orio.web_scraping_tool.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Orio.web_scraping_tool.model.DataModel;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    // Private Constructor to prevent instantiation
    private FileUtil() {
    };

    public static void saveResponseAsJson(Object response, String fileName) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            logger.debug("Saving response to file: {}", fileName);
            objectMapper.writeValue(new File(fileName), response);
            logger.debug("Successfully saved response to file: {}", fileName);
        } catch (IOException e) {
            logger.error("Error saving response to file: {}", fileName, e);
        }
    }

    public static void saveUrlContentAsJson(List<DataModel> dataList, String fileName) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            logger.debug("Saving URL content map to file: {}", fileName);
            objectMapper.writeValue(new File(fileName), dataList);
            logger.debug("Successfully saved URL content map to file: {}", fileName);
        } catch (IOException e) {
            logger.error("Error saving URL content map to file: {}", fileName, e);
        }
    }

    public static void createNewDataDir(String query) {
        // TODO create this method that will create new directory for search quuery,
        // where files from links will be downloaded
    }
}
