package com.Orio.wither_project.process.qa.config;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AITextFiltrationPromptConfig {

    private SystemMessage textFiltrationSystemMessage = new SystemMessage(
            """
                        Given the scraped data, analyze the content and determine if most of it contains valuable information. If it does, respond with binary value: 1 for true; 0 for false.
                    """);

    private SystemMessage textFiltrationResponseJsonSchema = new SystemMessage(
            """
                    Respond in the following json format: {
                        "is_valuable": "binary value"
                    }
                    """);

    private UserMessage userContentMessage = new UserMessage("""
            Tell me whether most of this fragment contains something pragmatically useful:\n\n \"\"\"[%s]\"\"\"
            """);

    public Prompt getTextFiltrationPrompt(String content) {
        return new Prompt(textFiltrationSystemMessage, textFiltrationResponseJsonSchema,
                new UserMessage(String.format(userContentMessage.getContent(), content)));
    }
}
