package com.Orio.wither_project.gather.config;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIPromptConfig {

    private SystemMessage qaExtractionSystemMessage = new SystemMessage(
            """
                        Given the scraped data, extract answers that are complete and encapsulate the idea fully. Then create the question the fragment answers.
                    """);

    private SystemMessage qaExtractionJsonSchema = new SystemMessage(
            """
                    Respond in the following json format: {
                        "qaPairs": [
                            {
                                "first_three_words_of_an_answer": "your first three words of a fragment that is the answer here",
                                "last_three_words_of_an_answer": "your last three words here",
                                "question": "your question here"
                            },
                            {
                                "first_three_words_of_an_answer": "first three words of another answer",
                                "last_three_words_of_an_answer": "last three words",
                                "question": "another question here"
                            }
                            // Additional Q&A pairs can be included here
                        ]
                    }
                    """);

    private UserMessage userContentMessage = new UserMessage("""
            Extract questions and answers out of this scraped data:\n\n \"\"\"[%s]\"\"\"
            """);

    public Prompt getQAExtractionPrompt(String content) {
        return new Prompt(qaExtractionSystemMessage, qaExtractionJsonSchema,
                new UserMessage(String.format(userContentMessage.getContent(), content)));
    }

}
