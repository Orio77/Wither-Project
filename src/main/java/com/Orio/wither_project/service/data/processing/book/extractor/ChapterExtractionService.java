package com.Orio.wither_project.service.data.processing.book.extractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.pdf.model.ChapterModel;
import com.Orio.wither_project.pdf.model.PageModel;

@Service
public class ChapterExtractionService {
    private static final Logger logger = LoggerFactory.getLogger(ChapterExtractionService.class);

    public enum ExtractionMode {
        FIND,
        MANUAL;

        public static ExtractionMode fromStartPages(List<Integer> startPages) {
            return startPages == null || startPages.isEmpty() ? FIND : MANUAL;
        }
    }

    public List<ChapterModel> getChapters(PDDocument doc, List<Integer> startPages) throws IOException {
        ExtractionMode mode = ExtractionMode.fromStartPages(startPages);
        logger.debug("Using {} mode for chapter extraction", mode);

        return switch (mode) {
            case FIND -> findChapters(doc);
            case MANUAL -> splitIntoChapters(doc, startPages);
        };
    }

    private List<ChapterModel> splitIntoChapters(PDDocument doc, List<Integer> chapterStartPages) throws IOException {
        if (chapterStartPages == null || chapterStartPages.isEmpty()) {
            throw new IllegalArgumentException("Chapter start pages must be provided for manual mode");
        }

        logger.debug("Starting manual chapter extraction from PDF document");
        List<ChapterModel> chapters = new ArrayList<>();
        PDFTextStripper stripper = new PDFTextStripper();
        int chapterCounter = 1;

        for (int i = 0; i < chapterStartPages.size(); i++) {
            int startPage = chapterStartPages.get(i);
            int endPage = i < chapterStartPages.size() - 1
                    ? chapterStartPages.get(i + 1) - 1
                    : doc.getNumberOfPages();

            ChapterModel chapter = new ChapterModel();
            chapter.setTitle("Chapter " + chapterCounter);
            chapter.setChapterNumber(chapterCounter++);
            List<PageModel> pages = new ArrayList<>();

            for (int pageNum = startPage; pageNum <= endPage; pageNum++) {
                stripper.setStartPage(pageNum);
                stripper.setEndPage(pageNum);
                String pageText = stripper.getText(doc);

                PageModel page = createPage(pageText, pageNum, chapter);
                pages.add(page);
            }

            finalizeChapter(chapter, pages);
            chapters.add(chapter);
        }

        logger.debug("Completed manual chapter extraction. Found {} chapters", chapters.size());
        return chapters;
    }

    private List<ChapterModel> findChapters(PDDocument doc) throws IOException {
        logger.debug("Starting automatic chapter extraction from PDF document");
        List<ChapterModel> chapters = new ArrayList<>();
        PDFTextStripper stripper = new PDFTextStripper();
        ChapterModel currentChapter = null;
        List<PageModel> currentPages = new ArrayList<>();
        int chapterCounter = 1;

        for (int i = 0; i < doc.getNumberOfPages(); i++) {
            stripper.setStartPage(i + 1);
            stripper.setEndPage(i + 1);
            String pageText = stripper.getText(doc);

            ChapterInfo chapterInfo = extractChapterInfo(pageText);

            if (i == 0 && chapterInfo == null) {
                chapterInfo = new ChapterInfo("Chapter 1", 1);
            }

            if (chapterInfo != null) {
                if (currentChapter != null) {
                    finalizeChapter(currentChapter, currentPages);
                    chapters.add(currentChapter);
                }

                currentChapter = createNewChapter(chapterInfo, chapterCounter++);
                currentPages = new ArrayList<>();
            }

            PageModel page = createPage(pageText, i + 1, currentChapter);
            currentPages.add(page);
        }

        if (currentChapter != null) {
            finalizeChapter(currentChapter, currentPages);
            chapters.add(currentChapter);
        }

        return chapters;
    }

    private ChapterModel createNewChapter(ChapterInfo info, int defaultNumber) {
        ChapterModel chapter = new ChapterModel();
        chapter.setTitle(info.title);
        chapter.setChapterNumber(info.number > 0 ? info.number : defaultNumber);
        return chapter;
    }

    private PageModel createPage(String content, int pageNumber, ChapterModel chapter) {
        PageModel page = new PageModel();
        page.setContent(content);
        page.setPageNumber(pageNumber);
        page.setChapter(chapter);
        return page;
    }

    private void finalizeChapter(ChapterModel chapter, List<PageModel> pages) {
        chapter.setPages(pages);
        chapter.setContent(buildChapterContent(pages));
    }

    private String buildChapterContent(List<PageModel> pages) {
        return pages.stream()
                .map(PageModel::getContent)
                .collect(Collectors.joining("\n"));
    }

    private ChapterInfo extractChapterInfo(String pageText) {
        String[] lines = pageText.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) { // Only check first 10 lines
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
