package com.Orio.wither_project.gader.service.orchestration;

import com.Orio.wither_project.gader.model.DataModel;
import com.Orio.wither_project.gader.model.DataSource;
import com.Orio.wither_project.gader.model.InformationPiece;
import com.Orio.wither_project.gader.model.ProcessResult;
import com.Orio.wither_project.gader.model.ScrapeResult;
import com.Orio.wither_project.gader.model.SearchResult;

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
