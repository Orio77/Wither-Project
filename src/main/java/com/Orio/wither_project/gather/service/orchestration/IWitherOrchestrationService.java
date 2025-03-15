package com.Orio.wither_project.gather.service.orchestration;

import com.Orio.wither_project.gather.model.DataModel;
import com.Orio.wither_project.gather.model.DataSource;
import com.Orio.wither_project.gather.model.InformationPiece;
import com.Orio.wither_project.gather.model.ProcessResult;
import com.Orio.wither_project.gather.model.ScrapeResult;
import com.Orio.wither_project.gather.model.SearchResult;

public interface IWitherOrchestrationService {

    default void orchestrate(String query) {
        SearchResult searchResult = search(query);
        DataSource dataSource = format(searchResult);
        ScrapeResult scrapeResult = scrape(dataSource);
        DataModel dataModel = format(scrapeResult);
        processAndSave(dataModel);
    }

    SearchResult search(String query);

    DataSource format(SearchResult searchResult);

    ScrapeResult scrape(DataSource dataSource);

    DataModel format(ScrapeResult scrapeResult);

    ProcessResult process(DataModel dataModel);

    InformationPiece format(ProcessResult processResult);

    InformationPiece save(InformationPiece informationPiece);

    void processAndSave(DataModel dataModel);

}
