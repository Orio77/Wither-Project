package com.Orio.web_scraping_tool.service.dataGathering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.Orio.web_scraping_tool.model.DataModel;
import com.Orio.web_scraping_tool.service.newImpl.dataGathering.JsoupScrapeService;

@SpringBootTest
public class JsoupScrapeServiceTest {

    @Autowired
    private JsoupScrapeService jsoupScrapeService;

    @Test
    public void testScrapeWithValidLinks() {
        List<String> links = Arrays.asList(
                "https://www.quora.com/What-is-the-difference-between-Sigmund-Freud-s-id-and-Carl-Jung-s-shadow-Or-does-the-shadow-reside-within-the-Id",
                "https://www.routledge.com/blog/article/what-is-jungian-psychology?srsltid=AfmBOopN0-_xcwV0U-qvFc2GOECyAmEqOgERoxW28VbkUcnHj8Oq9G25");
        List<DataModel> result = jsoupScrapeService.scrape(links);
        assertTrue(result != null && !result.isEmpty(), "Should return at least 1 result if not unlucky");
    }

    @Test
    public void testScrapeWithEmptyLinks() {
        List<String> links = Collections.emptyList();
        List<DataModel> result = jsoupScrapeService.scrape(links);
        assertEquals(0, result.size(), "Should return empty list for empty links");
    }

    @Test
    public void testScrapeWithInvalidLinks() {
        List<String> links = Arrays.asList("invalid-url");
        List<DataModel> result = jsoupScrapeService.scrape(links);
        assertEquals(0, result.size(), "Should return empty list for invalid links");
    }
}