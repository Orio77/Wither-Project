package com.Orio.wither_project.service.data.gathering.searching.impl;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.Orio.wither_project.config.GoogleSearchConfig;
import com.Orio.wither_project.exception.RateLimitReachedException;
import com.Orio.wither_project.exception.WebSearchException;
import com.Orio.wither_project.model.GoogleSearchResponseModel;
import com.Orio.wither_project.model.GoogleSearchResponseModel.Item;
import com.Orio.wither_project.service.data.gathering.searching.IWebSearchService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class GoogleSearchService implements IWebSearchService {

    private final GoogleSearchConfig googleSearchConfig;
    private final RestTemplate restTemplate;

    private static final Logger logger = getLogger(GoogleSearchService.class);

    private static final String NO_ITEMS_FOUND = "No items found in the response";
    private static final String QUERY_RECEIVED = "Received query for searching websites: {}";
    private static final String URL_CONSTRUCTED = "Constructed URL: {}";
    private static final String PROCESSING_LINK = "Processing link: {}";
    private static final String WEB_SEARCH_EXCEPTION = "Failed to execute Google search for query: ";

    @Override
    public List<String> getLinks(String query) throws RateLimitReachedException, WebSearchException {
        logger.info(QUERY_RECEIVED, query);

        GoogleSearchResponseModel response = executeGoogleSearch(query);

        return Optional.ofNullable(response)
                .map(GoogleSearchResponseModel::getItems)
                .map(this::processSearchResult)
                .orElseThrow(() -> {
                    logger.info(NO_ITEMS_FOUND);
                    return new WebSearchException("No items found for query: " + query);
                });
    }

    private GoogleSearchResponseModel executeGoogleSearch(String query) throws RateLimitReachedException {
        String url = buildSearchUrl(query);

        logger.debug(URL_CONSTRUCTED, url);
        try {
            // Set headers to mimic a browser
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", googleSearchConfig.getAgent());
            headers.set("Accept-Language", "en-US,en;q=0.9");
            headers.set("Accept", "application/json");

            logger.debug("Headers constructed: {}", headers);

            // Create the HTTP entity with headers
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Execute the request with headers
            ResponseEntity<GoogleSearchResponseModel> response = restTemplate.exchange(url, HttpMethod.GET, entity,
                    GoogleSearchResponseModel.class);

            return response.getBody();
        } catch (RestClientException e) {
            throw new RateLimitReachedException(WEB_SEARCH_EXCEPTION + query, e);
        }
    }

    private String buildSearchUrl(String query) {
        return UriComponentsBuilder.fromHttpUrl(googleSearchConfig.getSearchURL())
                .queryParam("key", googleSearchConfig.getApiKey())
                .queryParam("cx", googleSearchConfig.getSearchEngineId())
                .queryParam("q", query)
                .queryParam("count", googleSearchConfig.getNumResults())
                .queryParam("siteSearch", "youtube.com")
                .queryParam("siteSearchFilter", "e")
                .queryParam("safe", "off")
                .toUriString();
    }

    private List<String> processSearchResult(List<Item> items) {
        return items.stream().peek(item -> logger.debug("Title: {}", item.getTitle()))
                .map(Item::getLink)
                .filter(link -> link != null)
                .peek(link -> logger.debug(PROCESSING_LINK, link))
                .toList();
    }

    public List<String> searchPdfs(String query) {
        return new ArrayList<>();
    }
}