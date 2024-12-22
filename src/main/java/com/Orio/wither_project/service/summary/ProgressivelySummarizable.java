package com.Orio.wither_project.service.summary;

import java.util.List;

public interface ProgressivelySummarizable<T> {
    /**
     * Split the content into smaller parts for progressive summarization
     * 
     * @return List of parts to be summarized
     */
    List<T> split();

    /**
     * Get text content from the parts to be summarized
     * 
     * @param parts List of parts to extract text from
     * @return Combined text content
     */
    String getText(List<T> parts);

    /**
     * Set the content of the summary
     * 
     * @param content The summarized content
     */
    void setContent(String content);

    /**
     * Get the content of the summary
     * 
     * @return The summary content
     */
    String getContent();
}
