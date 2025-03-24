package com.Orio.wither_project.gather.service.search.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.Orio.wither_project.gather.exception.NoSearchResultsException;
import com.Orio.wither_project.gather.exception.SearchApiException;
import com.Orio.wither_project.gather.exception.SearchException;
import com.Orio.wither_project.gather.exception.SearchResponseParsingException;
import com.Orio.wither_project.gather.model.SearchResult;
import com.Orio.wither_project.gather.service.search.ISearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class GoogleSearchService implements ISearchService {

    private final RestTemplate restTemplate;

    @Value("${google.search.api.key}")
    private String apiKey;

    @Value("${google.search.engine.id}")
    private String searchEngineId;

    @Value("${google.search.api.url:https://www.googleapis.com/customsearch/v1}")
    private String apiUrl;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public SearchResult search(String query) {
        SearchResult result = SearchResult.builder().query(query).build();

        try {
            // Construct the request URL with query parameters
            String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                    .queryParam("key", apiKey)
                    .queryParam("cx", searchEngineId)
                    .queryParam("q", query)
                    .toUriString();

            // Make the request to Google API
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            // Process the response
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                // Process items from Google search results
                if (responseBody.containsKey("items")) {
                    processSearchItems(result, responseBody);
                } else {
                    result.addError(new NoSearchResultsException());
                }
            } else {
                int statusCode = response.getStatusCode().value();
                String errorMessage = "Failed to get search results: " + response.getStatusCode();
                log.error(errorMessage);
                result.addError(new SearchApiException(errorMessage, statusCode));
            }
        } catch (Exception e) {
            log.error("Error performing Google search: ", e);
            result.addError(new SearchException("Error performing Google search", e));
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private void processSearchItems(SearchResult result, Map<String, Object> responseBody) {
        try {
            // Cast the items to a list of maps
            java.util.List<Map<String, Object>> items = (java.util.List<Map<String, Object>>) responseBody.get("items");

            if (items != null) {
                // Process each item and create SearchResult.Item objects
                items.forEach(item -> {
                    try {
                        String title = (String) item.get("title");
                        String link = (String) item.get("link");

                        SearchResult.SearchItem resultItem = SearchResult.SearchItem.builder()
                                .title(title)
                                .link(link)
                                .build();

                        result.addSearchItem(resultItem);
                    } catch (Exception e) {
                        log.warn("Error processing search item: {}", e.getMessage());
                        result.addError(new SearchResponseParsingException(
                                "Failed to process search item: " + e.getMessage(), e));
                    }
                });
            } else {
                result.addError(new NoSearchResultsException("Search response contained no items array"));
            }
        } catch (ClassCastException e) {
            String errorMessage = "Error processing search items: Invalid response format";
            log.error(errorMessage, e);
            result.addError(new SearchResponseParsingException(errorMessage, e));
        } catch (Exception e) {
            log.error("Unexpected error processing search items: ", e);
            result.addError(new SearchException("Unexpected error processing search results", e));
        }
    }
}