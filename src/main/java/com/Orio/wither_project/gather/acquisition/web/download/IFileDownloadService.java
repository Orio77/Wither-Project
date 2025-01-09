package com.Orio.wither_project.gather.acquisition.web.download;

import java.util.List;

public interface IFileDownloadService {

    void download(List<String> links, String query);
}
