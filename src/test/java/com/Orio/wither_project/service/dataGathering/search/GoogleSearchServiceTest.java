package com.Orio.wither_project.service.dataGathering.search;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.Orio.wither_project.exception.RateLimitReachedException;
import com.Orio.wither_project.exception.WebSearchException;
import com.Orio.wither_project.gather.acquisition.web.search.impl.GoogleSearchServiceOld;

@SpringBootTest
public class GoogleSearchServiceTest {

    @Autowired
    private GoogleSearchServiceOld googleSearchService;

    @Test
    public void testGetLinks() throws WebSearchException {
        // Call the method to test
        List<String> links = null;
        try {
            links = googleSearchService.getLinks("How to make money online");
        } catch (RateLimitReachedException e) {
            System.out.println("Rate Limit reached. Cannot test the method");
        } catch (WebSearchException e) {
            System.out.println("Exception occurred: ");
            e.printStackTrace();
        }

        // Verify the results
        assertTrue(links != null);
        assertTrue(!links.isEmpty());
        System.out.println("Links: " + links);
    }
}