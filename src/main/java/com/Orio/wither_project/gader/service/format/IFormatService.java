package com.Orio.wither_project.gader.service.format;

import com.Orio.wither_project.gader.model.DataModel;
import com.Orio.wither_project.gader.model.DataSource;
import com.Orio.wither_project.gader.model.InformationPiece;
import com.Orio.wither_project.gader.model.ProcessResult;
import com.Orio.wither_project.gader.model.ScrapeResult;
import com.Orio.wither_project.gader.model.SearchResult;

public interface IFormatService {

    DataSource format(SearchResult searchResult);

    DataModel format(ScrapeResult scrapeResult);

    InformationPiece format(ProcessResult processResult);
}
