package com.Orio.web_scraping_tool.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ScrapyScrapingService {

    private final RestTemplate restTemplate;

    public Process connect() {
        System.out.println("PCS: Performing action... connecting to python server");
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("python",
                    "src/main/data_processing/scraper/python_server.py");
            processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            Process process = processBuilder.start();
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("PCS: Connected to python server successfully");
            return process;
        } catch (IOException e) {
            System.err.println("PCS: Failed to connect to python server");
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, String> sendLinks(List<String> webLinks) {
        System.out.println("PCS: Received webLinks: " + webLinks);
        connect();
        System.out.println("PCS: Performing action... sending links to the Python server");
        Map<String, String> scrapedData = scrapeData(webLinks);
        System.out.println("PCS: Done. Received scraped data from the Python Server: " + scrapedData.keySet().toString()
                + ", " + scrapedData.values().toString());

        return scrapedData;
    }

    private Map<String, String> scrapeData(List<String> links) {
        System.out.println("PCS: Received links: " + links);
        String pyURL = "http://localhost:5000/scrape";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        HttpEntity<List<String>> requestEntity = new HttpEntity<>(links, headers);
        System.out.println("PCS: Performing action... Sending http post request");
        ResponseEntity<Map<String, String>> response = restTemplate.exchange(pyURL, HttpMethod.POST, requestEntity,
                new ParameterizedTypeReference<Map<String, String>>() {
                });

        System.out.println("PCS: Done. Response received: " + response.getStatusCode());

        return response.getBody();
    }
}
