package com.Orio.wither_project.service.data.processing.book.extractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Orio.wither_project.model.ChapterModel;
import com.Orio.wither_project.model.PageModel;

@Service
public class ChapterExtracttionService {
    private static final Logger logger = LoggerFactory.getLogger(ChapterExtracttionService.class);

    public List<ChapterModel> getChapters(PDDocument doc) throws IOException {
        logger.debug("Starting chapter extraction from PDF document");
        List<ChapterModel> chapters = new ArrayList<>();
        PDFTextStripper stripper;
        try {
            stripper = new PDFTextStripper();
            ChapterModel currentChapter = null;
            List<PageModel> currentPages = new ArrayList<>();
            int chapterCounter = 1;

            int totalPages = doc.getNumberOfPages();
            logger.debug("Processing {} pages", totalPages);

            // Create initial chapter if none is detected on first page
            boolean firstPage = true;

            for (int i = 0; i < totalPages; i++) {
                stripper.setStartPage(i + 1);
                stripper.setEndPage(i + 1);
                String pageText = stripper.getText(doc);

                ChapterInfo chapterInfo = extractChapterInfo(pageText);

                // Handle first page or forced chapter creation
                if (firstPage) {
                    if (chapterInfo == null) {
                        chapterInfo = new ChapterInfo("Chapter 1", 1);
                    }
                    firstPage = false;
                }

                if (chapterInfo != null) {
                    logger.debug("Found new chapter start at page {}: {}", i + 1, chapterInfo.title);
                    if (currentChapter != null) {
                        currentChapter.setPages(currentPages);
                        chapters.add(currentChapter);
                        logger.trace("Added chapter with {} pages", currentPages.size());
                    }

                    currentChapter = new ChapterModel();
                    currentChapter.setTitle(chapterInfo.title);
                    currentChapter.setChapterNumber(chapterInfo.number > 0 ? chapterInfo.number : chapterCounter++);
                    currentPages = new ArrayList<>();
                }

                PageModel page = new PageModel();
                page.setContent(pageText);
                page.setPageNumber(i + 1);
                page.setChapter(currentChapter);
                currentPages.add(page);
            }

            // Add the last chapter if it exists
            if (currentChapter != null) {
                currentChapter.setPages(currentPages);
                chapters.add(currentChapter);
                logger.trace("Added final chapter with {} pages", currentPages.size());
            }

            logger.debug("Completed chapter extraction. Found {} chapters", chapters.size());

        } catch (IOException e) {
            logger.error("Failed to process PDF document", e);
            throw new RuntimeException("Failed to process PDF document", e);
        }

        return chapters;
    }

    private ChapterInfo extractChapterInfo(String pageText) {
        String[] lines = pageText.split("\\r?\\n");
        for (int i = 0; i < Math.min(10, lines.length); i++) { // Only check first 10 lines
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            // More flexible chapter patterns
            String[] patterns = {
                    "(?i)^chapter\\s*[\\d\\w]+.*", // Chapter 1, Chapter One
                    "(?i)^\\d+\\.?\\s+[A-Z].*", // 1. Title or 1 Title
                    "(?i)^[IVXivx]+\\.?\\s+[A-Z].*", // I. Title or I Title
                    "(?i)^part\\s+[\\d\\w]+.*", // Part 1, Part One
                    "(?i)^section\\s+[\\d\\w]+.*", // Section 1
                    "^[A-Z][A-Z\\s\\d]{2,}$" // UPPERCASE TITLE
            };

            for (String pattern : patterns) {
                if (line.matches(pattern)) {
                    String title = line;
                    int number = -1;

                    // Extract number if present
                    if (line.matches(".*\\d+.*")) {
                        try {
                            number = Integer.parseInt(line.replaceAll("\\D+", ""));
                        } catch (NumberFormatException e) {
                            // Ignore parsing errors
                        }
                    } else if (line.matches("(?i).*[IVXivx]+.*")) {
                        number = romanToArabic(line.replaceAll("[^IVXivx]+", ""));
                    }

                    logger.debug("Matched chapter pattern: '{}' -> number={}, title='{}'", pattern, number, title);
                    return new ChapterInfo(cleanTitle(title), number);
                }
            }
        }
        return null;
    }

    private String cleanTitle(String title) {
        // Remove common prefixes
        title = title.replaceAll("^(Chapter|CHAPTER|Part|PART)\\s+\\d*\\.?\\s*", "");
        // Remove multiple spaces
        title = title.replaceAll("\\s+", " ");
        // Remove common punctuation at the start/end
        title = title.replaceAll("^[\\s\\p{Punct}]+|[\\s\\p{Punct}]+$", "");
        return title.trim();
    }

    private int romanToArabic(String roman) {
        roman = roman.toUpperCase().trim();

        // Quick validation to ensure it's actually a Roman numeral
        if (!roman.matches("^[IVXLC]+$") || roman.equals("I")) {
            return -1;
        }

        Map<Character, Integer> romanMap = Map.of(
                'I', 1,
                'V', 5,
                'X', 10,
                'L', 50,
                'C', 100);

        int result = 0;
        int prevValue = 0;

        for (int i = roman.length() - 1; i >= 0; i--) {
            int currentValue = romanMap.getOrDefault(roman.charAt(i), 0);
            if (currentValue == 0)
                return -1; // Invalid Roman numeral

            if (currentValue >= prevValue) {
                result += currentValue;
            } else {
                result -= currentValue;
            }
            prevValue = currentValue;
        }

        return result;
    }

    private static class ChapterInfo {
        String title;
        int number;

        ChapterInfo(String title, int number) {
            this.title = title;
            this.number = number;
        }
    }
}
