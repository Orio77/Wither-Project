package com.Orio.wither_project.gather.service.format;

import java.util.List;

import com.Orio.wither_project.gather.model.Content;
import com.Orio.wither_project.gather.model.DataModel;
import com.Orio.wither_project.gather.model.DataSource;
import com.Orio.wither_project.gather.model.InformationPiece;
import com.Orio.wither_project.gather.model.ScrapeResult;
import com.Orio.wither_project.gather.model.SearchResult;

public interface IModelFormatService {

    DataSource format(SearchResult searchResult);

    DataModel format(ScrapeResult scrapeResult);

    List<InformationPiece> format(DataModel dataModel);

    Content format(InformationPiece item);

    default List<Content> format(List<InformationPiece> items) {
        return items.stream().map(this::format).toList();
    }

}
