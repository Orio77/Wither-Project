package com.Orio.wither_project.ai;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.Orio.wither_project.config.TestTextConfiguration;
import com.Orio.wither_project.core.config.OllamaConfig;

@SpringBootTest
public class OllamaMidSessionConfigChangeTest {

    @Autowired
    OllamaConfig ollamaConfig;

    @Autowired
    TestTextConfiguration texts;

    private final int NUM_ITERATIONS = 3;
    private final String USER_MESSAGE = "Summarize the following text:\n\n";

    private static final Logger logger = LoggerFactory.getLogger(OllamaMidSessionConfigChangeTest.class);

    @Test
    public void testChange() {

        OllamaChatModel ollamaChatModelLowNumCTX = ollamaConfig.getOllamaChatModelLowNumCTX();

        OllamaChatModel ollamaChatModelHighNumCTX = ollamaConfig.getOllamaChatModelHighNumCTX();

        String singleParagraph = texts.getSingleParagraph();

        Prompt prompt = new Prompt(List.of(new UserMessage(USER_MESSAGE),
                new UserMessage(singleParagraph)));

        long totalLengthHigh = 0;
        long startHigh = System.currentTimeMillis();
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            String response = ollamaChatModelHighNumCTX.call(prompt).getResult().getOutput().getContent();
            totalLengthHigh += response.length();
        }
        long endHigh = System.currentTimeMillis();
        double avgTimeHigh = (endHigh - startHigh) / (double) NUM_ITERATIONS / 1000.0;

        long totalLengthLow = 0;
        long startLow = System.currentTimeMillis();
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            String response = ollamaChatModelLowNumCTX.call(prompt).getResult().getOutput().getContent();
            totalLengthLow += response.length();
        }
        long endLow = System.currentTimeMillis();
        double avgTimeLow = (endLow - startLow) / (double) NUM_ITERATIONS / 1000.0;

        logger.info("High CTX - Average response length: {} characters, Average time: {} s",
                totalLengthHigh / NUM_ITERATIONS, avgTimeHigh);
        logger.info("Low CTX - Average response length: {} characters, Average time: {} s",
                totalLengthLow / NUM_ITERATIONS, avgTimeLow);
    }
}
