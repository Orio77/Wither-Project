package com.Orio.wither_project.service.dataGathering;

import java.util.List;

public interface IFileDownloadService {

    void download(List<String> links, String query);
}
