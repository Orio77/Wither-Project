package com.Orio.wither_project.service.data.gathering.scraping.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.model.DataModel;
import com.Orio.wither_project.service.data.gathering.scraping.IScrapeService;
import com.Orio.wither_project.util.FileUtil;

@Service
public class JsoupScrapeService implements IScrapeService {

    private static final Logger logger = LoggerFactory.getLogger(JsoupScrapeService.class);

    @Value("${webscraping.filename}")
    private String fileName;

    @Value("${webscraping.threadPoolSize}")
    private int threadPoolSize;

    @Value("${webscraping.executorTimeoutSeconds}")
    private int executorTimeoutSeconds;

    @Value("${webscraping.userAgent}")
    private String userAgent;

    @Value("${webscraping.maxContentSize}")
    private int maxContentSize;

    @Override
    public List<DataModel> scrape(List<String> links) {
        logger.info("Received {} links for scraping", links);
        logger.info("Filename: {}", fileName);
        logger.info("Thread pool size: {}", threadPoolSize);
        logger.info("Executor timeout (seconds): {}", executorTimeoutSeconds);
        logger.info("User agent: {}", userAgent);
        logger.info("Max content size: {}", maxContentSize);

        List<DataModel> dataList = new ArrayList<>();

        if (threadPoolSize <= 0) {
            throw new IllegalArgumentException("Thread pool size must be greater than 0");
        }

        try {
            processSearchResults(links, dataList);
            FileUtil.saveUrlContentAsJson(dataList, fileName);
        } catch (InterruptedException e) {
            logger.error("Thread was interrupted", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Unexpected error occurred while searching websites", e);
        }

        return dataList;
    }

    private void processSearchResults(List<String> websiteLinks, List<DataModel> dataList)
            throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(getOptimalThreadPoolSize());

        try {
            for (String link : websiteLinks) {
                executor.submit(() -> scrapeAndStoreContent(link, dataList));
            }
        } finally {
            shutdownExecutor(executor);
        }
    }

    private int getOptimalThreadPoolSize() {
        return Runtime.getRuntime().availableProcessors();
    }

    private void scrapeAndStoreContent(String link, List<DataModel> dataList) {
        try {
            Document doc = Jsoup.connect(link).userAgent(userAgent).get();
            String content = doc.text();
            for (int i = 0; i < content.length(); i += maxContentSize) {
                String chunk = content.substring(i, Math.min(content.length(), i + maxContentSize));
                dataList.add(new DataModel(link, chunk));
            }
            logger.debug("Successfully fetched content from: {}", link);
        } catch (HttpStatusException e) {
            logger.error("Failed to fetch URL: {} with status: {}", e.getUrl(), e.getStatusCode());
        } catch (IOException e) {
            logger.error("Error fetching content from URL: {}", link, e);
        }
    }

    private void shutdownExecutor(ExecutorService executor) throws InterruptedException {
        executor.shutdown();
        if (!executor.awaitTermination(executorTimeoutSeconds, TimeUnit.SECONDS)) {
            executor.shutdownNow();
            if (!executor.awaitTermination(executorTimeoutSeconds, TimeUnit.SECONDS)) {
                logger.error("Executor did not terminate properly.");
            }
        }
    }
}
