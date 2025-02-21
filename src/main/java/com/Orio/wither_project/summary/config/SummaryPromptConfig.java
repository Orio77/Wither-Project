package com.Orio.wither_project.summary.config;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "summary.prompts")
@Data
public class SummaryPromptConfig {
    private String page = "Summarize this page.";
    private String chapter = "Summarize this chapter.";
    private String book = "Summarize this book.";
    private String defaultPrompt = "Summarize the text.";

    private String chapterInitial = "Generate an initial chapter summary from this first section. This will be expanded with more sections: ";
    private String bookInitial = "Generate an initial book summary from this first chapter. This will be expanded with more chapters: ";
    private String defaultInitial = "Generate an initial summary from this section: ";

    private String chapterExpand = "Given the current chapter summary: [%s]\nExpand this summary by incorporating this next section: ";
    private String bookExpand = "Given the current book summary: [%s]\nExpand this summary by incorporating this next chapter: ";
    private String defaultExpand = "Given the current summary: [%s]\nExpand this summary by incorporating this next part: ";

    private SystemMessage summaryJsonSchema = new SystemMessage("""
                Apply the following json schema for your response:
            {
                "summary": "your summary of the given passage"
            }

            Note: Make 'summary' the only node.
            """);

    private SystemMessage continuousSummarySystemMessage = new SystemMessage("""
            Your summary must be a continuous text.
            """);

    private String progressiveSummaryJsonSchema = """
            {
                "summary": "your summary of the new parts, that will be appended to the already-existing summary, making a coherent text"
            }
            """;

    // For Prompt engineering:

    // New Executive Summary prompts
    private String executiveSummaryJsonSchema = """
            {
                "summary": "a concise, high-level overview highlighting strategic insights and key points of the given passage"
            }
            """;

    private SystemMessage executiveSummarySystemMessage = new SystemMessage(
            """
                        Act as an executive summary writer. Provide a concise, high-level overview of the given passage, focusing on strategic insights and key points without delving into technical details.
                    """);

    // New Detailed Technical Summary prompts
    private SystemMessage detailedTechnicalSummaryJsonSchema = new SystemMessage(
            """
                    Apply the following json schema for your response:
                    {
                        "summary": "a comprehensive, technically detailed summary of the given passage, capturing all relevant technical information and nuances"
                    }
                    """);

    private SystemMessage detailedTechnicalSummarySystemMessage = new SystemMessage(
            """
                        Act as a detailed technical summarizer. Provide a comprehensive summary of the given passage, capturing all relevant technical information and nuances with precision.
                    """);

    // New Creative Summary prompts
    private String creativeSummaryJsonSchema = """
            {
                "summary": "an engaging and creatively written summary of the given passage that maintains the essence while enhancing readability and interest"
            }
            """;

    private SystemMessage creativeSummarySystemMessage = new SystemMessage(
            """
                        Act as a creative summarizer. Provide an engaging and creatively written summary of the given passage that maintains the essence of the original text while enhancing readability and interest.
                    """);

    // New Analytical Summary prompts
    private String analyticalSummaryJsonSchema = """
            {
                "summary": "an analytical summary of the given passage that condenses the information and provides insights into its implications or significance"
            }
            """;

    private SystemMessage analyticalSummarySystemMessage = new SystemMessage(
            """
                        Act as an analytical summarizer. Provide a summary of the given passage that not only condenses the information but also offers insights into its implications or significance.
                    """);

    // New Narrative Summary prompts
    private String narrativeSummaryJsonSchema = """
            {
                "summary": "a narrative-style summary of the given passage that presents the information in a storytelling format while maintaining factual accuracy"
            }
            """;

    private SystemMessage narrativeSummarySystemMessage = new SystemMessage(
            """
                        Act as a narrative summarizer. Provide a summary of the given passage in a storytelling format that enhances engagement while maintaining factual accuracy.
                    """);
}