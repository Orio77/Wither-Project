package com.Orio.wither_project.gather.service.orchestration;

import java.util.List;

import com.Orio.wither_project.gather.model.DataModel;
import com.Orio.wither_project.gather.model.InformationPiece;
import com.Orio.wither_project.gather.model.ScrapeResult;
import com.Orio.wither_project.gather.model.dto.QueryRequest;
import com.Orio.wither_project.gather.model.dto.WebRequest;
import com.Orio.wither_project.pdf.model.entity.FileEntity;

public interface IGatherOrchestrationService {

    default List<InformationPiece> gather(String query) {
        DataModel res = gatherData(query);
        DataModel accepted = sendAndWait(res);
        List<InformationPiece> info = format(accepted);
        save(info);

        return info;
    }

    default List<InformationPiece> orchestrate(QueryRequest request) {
        if (request == null) {
            // TODO throw custom exception
        }

        String query = request.getQuery();
        WebRequest webRequest = request.getUrl();
        List<WebRequest> webRequests = request.getUrls();
        FileEntity file = request.getFile();
        String content = request.getContent();

        if (query != null) {
            return gather(query);
        } else if (webRequest != null) {
            addData(webRequest);
        } else if (webRequests != null) {
            addData(webRequests);
        } else if (file != null) {
            addData(file);
        } else if (content != null) {
            addData(content);
        }

        return null; // TODO throw custom exception
    }

    default void addData(WebRequest request) {
        ScrapeResult res = scrape(request.getUrl());
        DataModel dataModel = format(res);
        dataModel.getItems().stream().forEach(item -> item.setTitle(request.getTitle()));
        List<InformationPiece> info = format(dataModel);
        save(info);
    }

    default void addData(List<WebRequest> requests) {
        for (WebRequest request : requests) {
            addData(request);
        }
    }

    default void addData(FileEntity file) {
        // TODO
    }

    default void addData(String content) {
        // TODO
    }

    DataModel gatherData(String query);

    DataModel sendAndWait(DataModel dataModel);

    List<InformationPiece> format(DataModel dataModels);

    void save(List<InformationPiece> dataModels);

    ScrapeResult scrape(String url);

    DataModel format(ScrapeResult scrapeResult);

}
