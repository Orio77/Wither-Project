package com.Orio.wither_project.process.qa.config;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SuppressWarnings("unused")
public class AIQAPromptConfig {

    // --- QA Extraction Prompts ---
    private final SystemMessage qaExtractionSystemMessage = new SystemMessage(
            """
                    Given the scraped data, extract answers that are complete and encapsulate an idea fully.
                    Then create the question the fragment answers.
                    """);

    private final SystemMessage qaExtractionJsonSchema = new SystemMessage(
            """
                    Respond in the following JSON format:
                    {
                        "qaPairs": [
                            {
                                "first_three_words_of_an_answer": "your first three words of a fragment that is the answer here",
                                "last_three_words_of_an_answer": "your last three words here",
                                "question": "your question here"
                            }
                            // Additional Q&A pairs can be included here
                        ]
                    }
                    """);

    // --- Question Generation Prompts ---
    private final SystemMessage questionGenerationSystemMessage = new SystemMessage(
            """
                    You will receive a complete text for analysis.

                    Instructions:
                    - Generate diverse, thought-provoking questions that can be answered exclusively from the provided text.
                    - Create questions at different cognitive levels (factual recall, comprehension, analysis, synthesis).
                    - Focus on questions that demonstrate deep understanding of key concepts, relationships, and implications.
                    - Ensure questions target important information rather than peripheral details.
                    - Craft questions that would require detailed, substantive answers from the text.
                    - Avoid questions that can be answered with a simple yes/no or that require external information.
                    - Balance questions across different sections of the text to ensure comprehensive coverage.
                    """);

    private final SystemMessage questionGenerationJsonSchema = new SystemMessage(
            """
                    Respond in the following JSON format:
                    {
                        "questions": [
                            "Generated question 1?",
                            "Generated question 2?"
                            // Additional questions
                        ]
                    }
                    """);

    // --- Answer Generation Prompts ---
    private final SystemMessage answerGenerationSystemMessage = new SystemMessage("""
            You will receive:
            - A complete text.
            - A specific question.

            Instructions:
            - Find and extract the answer **from the text only**.
            - The user prefers longer, comprehensive answers — include as much relevant detail as possible.
            - Use [...] if you skip over parts of the answer, but keep it cohesive.
            - Do not paraphrase, do not invent, and do not summarize. Use the text word-for-word.

            Provide only the following JSON output:
            {
                "question": "The original question provided",
                "answer": "Your extracted answer here, using only text from the source"
            }
            """);

    private final SystemMessage refinementSystemMessage = new SystemMessage(
            """
                    You will receive:
                    - A complete text (context).
                    - An existing question.
                    - An existing answer extracted from the text.

                    Instructions:
                    - Review the existing question and answer based *only* on the provided text context.
                    - Refine the *answer* to be more complete and comprehensive, ensuring it directly addresses the question using only information found in the text.
                    - Maintain the original phrasing from the text as much as possible. Use [...] for omissions if necessary.
                    - Do not add external information or summaries.
                    - If the original answer is already good and accurate based on the text, return it unchanged.
                    - Keep the original question unchanged.

                    Provide only the following JSON output:
                    {
                        "question": "The original question provided",
                        "answer": "Your refined (or original) answer here, using only text from the source"
                    }
                    """);

    // --- Common User Messages ---
    private final String userContentFormat = """
            Process the following text:
            \"\"\"
            %s
            \"\"\"
            """;

    private final String userTextQuestionFormat = """
            Text:
            \"\"\"
            %s
            \"\"\"

            Question:
            \"\"\"
            %s
            \"\"\"
            """;

    private final String userRefinementFormat = """
            Text Context:
            \"\"\"
            %s
            \"\"\"

            Existing Question:
            \"\"\"
            %s
            \"\"\"

            Existing Answer:
            \"\"\"
            %s
            \"\"\"
            """;

    public Prompt getQAExtractionPrompt(String content) {
        var userMessage = new UserMessage(userContentFormat.formatted(content));
        return new Prompt(List.of(qaExtractionSystemMessage, qaExtractionJsonSchema, userMessage));
    }

    public Prompt getQuestionGenerationPrompt(String content) {
        var userMessage = new UserMessage(userContentFormat.formatted(content));
        return new Prompt(List.of(questionGenerationSystemMessage, questionGenerationJsonSchema, userMessage));
    }

    public Prompt getAnswerGenerationPrompt(String content, String question) {
        var userMessage = new UserMessage(userTextQuestionFormat.formatted(content, question));
        return new Prompt(List.of(answerGenerationSystemMessage, userMessage));
    }

    public Prompt getRefinementPrompt(String content, String question, String answer) {
        var userMessage = new UserMessage(userRefinementFormat.formatted(content, question, answer));
        return new Prompt(List.of(refinementSystemMessage, userMessage));
    }
}