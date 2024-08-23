package com.Orio.web_scraping_tool.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Orio.web_scraping_tool.model.DataModel;
import com.Orio.web_scraping_tool.service.IQueryHandlerService;
// import com.Orio.web_scraping_tool.service.impl.QueryHandlerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class WebScrapingController {

    // private final QueryHandlerService queryHandlerService;
    private final IQueryHandlerService queryHandlerService2;

    // @GetMapping("/query")
    // public Map<String, String> getAnswers(@RequestParam String question) {
    // System.out.println("QC: Query received");
    // Map<String, String> receivedData = queryHandlerService.handle(question);
    // System.out.println("QC: Received Data from queryHandler: " + receivedData);
    // return new HashMap<>();
    // }

    @GetMapping("/gather")
    public List<DataModel> gatherData(@RequestParam String query) {
        List<DataModel> data = queryHandlerService2.getData(query);
        queryHandlerService2.processData(data);
        queryHandlerService2.saveData(data);

        return data;
    }

}
