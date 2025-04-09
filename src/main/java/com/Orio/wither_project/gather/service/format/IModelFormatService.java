package com.Orio.wither_project.gather.service.format;

import java.util.List;

import com.Orio.wither_project.gather.model.ContentWithSource;
import com.Orio.wither_project.gather.model.DataModel;
import com.Orio.wither_project.gather.model.DataSource;
import com.Orio.wither_project.gather.model.InformationPiece;
import com.Orio.wither_project.gather.model.ScrapeResult;
import com.Orio.wither_project.gather.model.SearchResult;

public interface IModelFormatService {

    DataSource format(SearchResult searchResult);

    DataModel format(ScrapeResult scrapeResult);

    List<InformationPiece> format(DataModel dataModel);

    ContentWithSource format(InformationPiece item);

    default List<ContentWithSource> format(List<InformationPiece> items) {
        return items.stream().map(this::format).toList();
    }

}
