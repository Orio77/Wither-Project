// package com.Orio.web_scraping_tool.service.impl;

// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import com.Orio.web_scraping_tool.exception.WebSearchException;
// import com.Orio.web_scraping_tool.model.DataModel;
// import com.Orio.web_scraping_tool.service.dataSaving.IVectorStoreService;
// import
// com.Orio.web_scraping_tool.service.newImpl.dataGathering.JsoupScrapeService;
// import
// com.Orio.web_scraping_tool.service.newImpl.dataGathering.search.GoogleSearchService;
// import
// com.Orio.web_scraping_tool.service.newImpl.dataProcessing.OllamaQAService;

// import lombok.AllArgsConstructor;

// @Service
// @AllArgsConstructor(onConstructor = @__(@Autowired))
// public class QueryHandlerService {

// private static final Logger logger =
// LoggerFactory.getLogger(QueryHandlerService.class);

// private final GoogleSearchService googleSearchService;
// private final JsoupScrapeService jsoupScrapingService;
// private final OllamaQAService aiService;
// private final SQLService sqlService;
// private final IVectorStoreService vectorDbService;

// @SuppressWarnings("unused")
// public Map<String, String> handle(String question) {
// logger.info("QHS: Received query question: {}", question);

// logger.info("QHS: Searching the web...");
// List<String> websiteLinks = new ArrayList<>();
// try {
// websiteLinks = googleSearchService.getLinks(question); // Done
// } catch (WebSearchException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// }
// List<DataModel> webSiteData =
// jsoupScrapingService.scrape(websiteLinks.stream().limit(3).toList()); // Done
// List<String> pdfLinks = googleSearchService.searchPdfs(question); // TODO
// Map<String, String> ytTranscripts; // TODO
// logger.info("QHS: Done Searching the web");

// logger.info("QHS: Scraping the pdfs for information");
// Map<String, String> pdfData; // TODO
// logger.info("QHS: Done scraping pdfs for information");

// // TODO Get Data From Wikipedia

// // TODO Get Data From Core AI

// logger.info("QHS: Creating questions and answers with AI...");
// aiService.generateQuestions(webSiteData);
// logger.info("QHS: Done. Questions and answers created");

// logger.info("QHS: Saving Data to SQL database...");
// if (webSiteData != null && !webSiteData.isEmpty()) {
// sqlService.saveAll(webSiteData.stream().filter(data -> data.getAnswer() !=
// null).toList());
// logger.info("Done. Data Saved to SQL successfully");
// vectorDbService.save(
// webSiteData.stream().filter(data -> data.getAnswer() != null).toList());
// logger.info("Done. Data Saved to VectorDB successfully");
// } else {
// logger.error("NOT Done. Data to be saved to SQL and VectorDB was null or
// empty: {}", webSiteData);
// }

// return new HashMap<>();
// }
// }