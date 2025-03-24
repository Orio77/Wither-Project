package com.Orio.wither_project.gather.service.orchestration;

import java.util.List;

import com.Orio.wither_project.gather.model.DataModel;
import com.Orio.wither_project.gather.model.InformationPiece;
import com.Orio.wither_project.gather.model.ScrapeResult;

public interface INewWitherOrchestrationService {

    default List<InformationPiece> gather(String query) {
        DataModel res = gatherData(query);
        DataModel accepted = sendAndWait(res);
        List<InformationPiece> info = format(accepted);
        save(info);

        return info;
    }

    default void process(Long id) {
        DataModel data = getDataModel(id);
        processAndSave(data);
    }

    default void addData(String url, String title) {
        ScrapeResult res = scrape(url);
        DataModel dataModel = format(res);
        save(dataModel);
    }

    DataModel gatherData(String query);

    DataModel sendAndWait(DataModel dataModel);

    List<InformationPiece> format(DataModel dataModels);

    void save(List<InformationPiece> dataModels);

    DataModel getDataModel(Long id);

    void processAndSave(DataModel dataModel);

    ScrapeResult scrape(String url);

    DataModel format(ScrapeResult scrapeResult);

    void save(DataModel dataModel);
}
