package com.Orio.web_scraping_tool.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ThreadUtilTest {

    @Test
    public void testGetNumAvailableThreads() {
        int availableThreads = ThreadUtil.getNumAvailableThreads();
        System.out.println("Available Threads: " + availableThreads);

        int allThreads = Runtime.getRuntime().availableProcessors();
        System.out.println("All threads: " + allThreads);
        // Assuming there should be at least one available thread
        assertTrue(availableThreads > 0, "There should be at least one available thread");
    }
}