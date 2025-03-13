package com.Orio.wither_project.gader.service.scrape.impl;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.gader.config.ScrapeConfig;
import com.Orio.wither_project.gader.model.DataSource;
import com.Orio.wither_project.gader.model.ScrapeResult;
import com.Orio.wither_project.gader.model.ScrapeResult.ScrapeItem;
import com.Orio.wither_project.gader.model.SearchResult.SearchItem;
import com.Orio.wither_project.gader.service.scrape.IScrapeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicScrapeService implements IScrapeService {

    private final ScrapeConfig config;

    @Override
    public ScrapeResult scrape(DataSource dataSource) {
        Instant startTime = Instant.now();
        log.info("Starting scraping process for query: '{}' with {} items",
                dataSource.getQuery(),
                dataSource.getItems() != null ? dataSource.getItems().size() : 0);

        ScrapeResult result = ScrapeResult.builder()
                .query(dataSource.getQuery())
                .build();

        if (dataSource.getItems() == null || dataSource.getItems().isEmpty()) {
            log.warn("No items to scrape in the data source for query: '{}'", dataSource.getQuery());
            return result;
        }

        int itemCount = dataSource.getItems().size();
        log.info("Preparing to scrape {} URLs for query: '{}'", itemCount, dataSource.getQuery());

        // Create thread pool with named threads for better debugging
        int poolSize = config.getThreadPoolSize();
        log.debug("Configured thread pool size: {}", poolSize);
        log.debug("Items to scrape: {}", itemCount);
        int threadCount = Math.min(poolSize, itemCount);
        log.debug("Creating thread pool with {} threads", threadCount);

        ExecutorService executor = Executors.newFixedThreadPool(
                threadCount,
                r -> {
                    Thread t = new Thread(r, "scraper-thread");
                    t.setDaemon(true);
                    return t;
                });

        try {
            log.debug("Submitting scrape tasks to executor");
            List<CompletableFuture<ScrapeItem>> futures = dataSource.getItems().stream()
                    .map(searchItem -> CompletableFuture.supplyAsync(() -> scrapeItem(searchItem), executor))
                    .collect(Collectors.toList());

            // Wait for all futures to complete
            CompletableFuture<Void> allOf = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0]));

            log.debug("Waiting for all {} scraping tasks to complete (timeout: {}s)",
                    futures.size(),
                    config.getTimeoutSeconds() * dataSource.getItems().size());

            try {
                allOf.get(config.getTimeoutSeconds() * dataSource.getItems().size(), TimeUnit.SECONDS);
                log.debug("All scraping tasks completed");

                // Process completed futures
                int successCount = 0;
                int errorCount = 0;

                log.debug("Processing scraping results");
                for (CompletableFuture<ScrapeItem> future : futures) {
                    try {
                        ScrapeItem scrapeItem = future.get();
                        if (scrapeItem != null) {
                            if (scrapeItem.getError() == null) {
                                result.addItem(scrapeItem);
                                successCount++;
                            } else {
                                result.addError(scrapeItem.getError());
                                errorCount++;
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error retrieving scrape result: {}", e.getMessage(), e);
                        result.addError(e);
                        errorCount++;
                    }
                }

                log.info("Scraping completed: {} successful, {} failed", successCount, errorCount);

            } catch (Exception e) {
                log.error("Timeout or error while scraping data: {}", e.getMessage(), e);
                result.addError(e);
            }
        } finally {
            log.debug("Shutting down scraping thread pool");
            shutdownAndAwaitTermination(executor);
        }

        Duration duration = Duration.between(startTime, Instant.now());
        log.info("Scraping process for query '{}' completed in {}ms. Results: {} items, {} errors",
                dataSource.getQuery(),
                duration.toMillis(),
                result.getItems().size(),
                result.getErrors().size());

        return result;
    }

    private ScrapeItem scrapeItem(SearchItem searchItem) {
        Instant startTime = Instant.now();
        String url = searchItem.getLink();
        log.info("Starting to scrape content from: {}", url);

        ScrapeItem scrapeItem = ScrapeItem.builder().link(url).title(searchItem.getTitle()).build();

        // Implement retry logic with exponential backoff
        for (int attempt = 0; attempt < config.getMaxRetries(); attempt++) {
            try {
                // Add backoff delay for retries
                if (attempt > 0) {
                    long delayMs = (long) (Math.pow(2, attempt) * 500 + Math.random() * 500);
                    log.debug("Retry {} for {}: waiting {}ms before retry", attempt + 1, url, delayMs);
                    Thread.sleep(delayMs);
                }

                log.debug("Connecting to URL: {} (attempt {}/{})", url, attempt + 1, config.getMaxRetries());
                Instant fetchStartTime = Instant.now();

                Document doc = Jsoup.connect(url)
                        .userAgent(config.getUserAgent())
                        .timeout((int) Duration.ofSeconds(config.getTimeoutSeconds()).toMillis())
                        .get();

                Duration fetchDuration = Duration.between(fetchStartTime, Instant.now());
                log.debug("Successfully fetched document from {} in {}ms", url, fetchDuration.toMillis());

                // Extract useful content from the page
                log.debug("Extracting content from {}", url);
                Instant extractStartTime = Instant.now();
                extractContent(doc, scrapeItem);
                Duration extractDuration = Duration.between(extractStartTime, Instant.now());

                Duration totalDuration = Duration.between(startTime, Instant.now());
                log.info("Successfully scraped {} in {}ms (fetch: {}ms, extract: {}ms)",
                        url,
                        totalDuration.toMillis(),
                        fetchDuration.toMillis(),
                        extractDuration.toMillis());

                // Log content statistics
                logContentStatistics(scrapeItem);

                return scrapeItem;
            } catch (IOException e) {
                if (attempt == config.getMaxRetries() - 1) {
                    log.error("Failed to scrape content from: {} after {} attempts. Error: {}",
                            url, config.getMaxRetries(), e.getMessage(), e);
                    scrapeItem.setError(e);
                } else {
                    log.warn("Attempt {}/{} failed for URL: {}. Error: {}",
                            attempt + 1, config.getMaxRetries(), url, e.getMessage());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Scraping interrupted for URL: {}", url, e);
                scrapeItem.setError(e);
                return scrapeItem;
            } catch (Exception e) {
                log.error("Unexpected error while scraping URL: {}. Error: {}", url, e.getMessage(), e);
                scrapeItem.setError(e);
                return scrapeItem;
            }
        }

        Duration duration = Duration.between(startTime, Instant.now());
        log.warn("Failed to scrape {} after {}ms and {} attempts", url, duration.toMillis(), config.getMaxRetries());
        return scrapeItem;
    }

    private void extractContent(Document doc, ScrapeItem item) {
        log.debug("Extracting title from document");
        // Extract page title
        String title = doc.title();
        if (item.getTitle() == null || item.getTitle().isEmpty()) {
            item.setTitle(title);
            log.trace("Extracted title: {}", title);
        } else {
            log.trace("Keeping existing title: {}", item.getTitle());
        }

        log.debug("Extracting meta description");
        // Extract meta description
        String description = doc.select("meta[name=description]").attr("content");
        item.setDescription(description);
        log.trace("Extracted description: {}",
                description.length() > 100 ? description.substring(0, 97) + "..." : description);

        log.debug("Extracting author information");
        // Extract author information
        String author = extractAuthor(doc);
        item.setAuthor(author);
        if (!author.isEmpty()) {
            log.trace("Extracted author: {}", author);
        } else {
            log.trace("No author information found");
        }

        log.debug("Extracting publish date");
        // Extract publish date
        String publishDate = extractPublishDate(doc);
        item.setPublishDate(publishDate);
        if (!publishDate.isEmpty()) {
            log.trace("Extracted publish date: {}", publishDate);
        } else {
            log.trace("No publish date found");
        }

        log.debug("Extracting main content");
        // Extract main content
        String content = extractMainContent(doc);
        item.setContent(content);
        log.trace("Extracted content length: {} characters", content.length());
    }

    private String extractMainContent(Document doc) {
        log.debug("Trying content extraction strategy 1: Common content containers");
        // Strategy 1: Look for common content containers
        for (String selector : config.getContentSelectors().getContentContainers()) {
            Elements elements = doc.select(selector);
            if (!elements.isEmpty()) {
                log.debug("Found content using selector: {}", selector);
                // Clean up the content - remove scripts, styles, etc.
                elements.select("script, style, .comments, .comment, nav, .sidebar, .footer, .header, .navigation")
                        .remove();
                log.debug("Found content: {}", elements.text());
                return elements.text();
            }
        }

        log.debug("Trying content extraction strategy 2: Element with most paragraphs");
        // Strategy 2: Find the element with the most paragraphs
        Element bestElement = null;
        int maxParagraphs = 0;

        for (Element element : doc.select("div, section, article")) {
            int paragraphs = element.select("p").size();
            if (paragraphs > maxParagraphs) {
                maxParagraphs = paragraphs;
                bestElement = element;
            }
        }

        if (bestElement != null && maxParagraphs > 2) {
            log.debug("Found content container with {} paragraphs", maxParagraphs);
            log.debug("Found content: {}", bestElement.text());
            return bestElement.text();
        }

        log.debug("Using fallback content extraction: Body text");
        // Fallback: Use the body content (limited length)
        String bodyText = doc.body().text();
        log.debug("Found content: {}", bodyText);
        return bodyText;
    }

    private String extractAuthor(Document doc) {
        // Common author selectors
        for (String selector : config.getContentSelectors().getAuthorSelectors()) {
            Elements elements = doc.select(selector);
            if (!elements.isEmpty()) {
                String author;
                if (selector.startsWith("meta")) {
                    author = elements.attr("content");
                } else {
                    author = elements.first().text();
                }
                log.trace("Found author using selector {}: {}", selector, author);
                return author;
            }
        }
        log.trace("No author information found with common selectors");
        return "";
    }

    private String extractPublishDate(Document doc) {
        // Common date selectors
        for (String selector : config.getContentSelectors().getDateSelectors()) {
            Elements elements = doc.select(selector);
            if (!elements.isEmpty()) {
                String date;
                if (selector.startsWith("meta")) {
                    date = elements.attr("content");
                } else if (elements.hasAttr("datetime")) {
                    date = elements.attr("datetime");
                } else {
                    date = elements.first().text();
                }
                log.trace("Found publish date using selector {}: {}", selector, date);
                return date;
            }
        }
        log.trace("No publish date found with common selectors");
        return "";
    }

    private void logContentStatistics(ScrapeItem item) {
        StringBuilder stats = new StringBuilder("Content statistics for ").append(item.getLink()).append(":");
        stats.append(" title=").append(item.getTitle() != null ? item.getTitle().length() : 0).append("chars");
        stats.append(" description=").append(item.getDescription() != null ? item.getDescription().length() : 0)
                .append("chars");
        stats.append(" content=").append(item.getContent() != null ? item.getContent().length() : 0).append("chars");
        stats.append(" author=").append(item.getAuthor() != null && !item.getAuthor().isEmpty() ? "yes" : "no");
        stats.append(" publishDate=")
                .append(item.getPublishDate() != null && !item.getPublishDate().isEmpty() ? "yes" : "no");

        log.info(stats.toString());
    }

    private void shutdownAndAwaitTermination(ExecutorService executor) {
        log.debug("Initiating shutdown of scraper thread pool");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(config.getTimeoutSeconds(), TimeUnit.SECONDS)) {
                log.warn("Thread pool did not terminate in {} seconds, forcing shutdown", config.getTimeoutSeconds());
                executor.shutdownNow();
                if (!executor.awaitTermination(config.getTimeoutSeconds(), TimeUnit.SECONDS)) {
                    log.error("Thread pool did not terminate even after forced shutdown");
                } else {
                    log.debug("Thread pool terminated after forced shutdown");
                }
            } else {
                log.debug("Thread pool terminated successfully");
            }
        } catch (InterruptedException ie) {
            log.warn("Shutdown of scraper thread pool was interrupted", ie);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}