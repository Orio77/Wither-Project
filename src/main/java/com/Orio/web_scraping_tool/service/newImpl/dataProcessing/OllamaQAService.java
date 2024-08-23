package com.Orio.web_scraping_tool.service.newImpl.dataProcessing;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.ollama.api.OllamaApi.ChatRequest;
import org.springframework.ai.ollama.api.OllamaApi.ChatResponse;
import org.springframework.ai.ollama.api.OllamaApi.Message;
import org.springframework.ai.ollama.api.OllamaApi.Message.Role;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;

import com.Orio.web_scraping_tool.config.OllamaConfig;
import com.Orio.web_scraping_tool.model.DataModel;
import com.Orio.web_scraping_tool.model.OllamaThreeWordsResponseModel;
import com.Orio.web_scraping_tool.service.dataProcessing.IAIQAService;
import com.Orio.web_scraping_tool.util.TextUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OllamaQAService implements IAIQAService {

        private final OllamaConfig ollamaConfig;
        private final ObjectMapper objMapper;
        private static final Logger logger = LoggerFactory.getLogger(OllamaQAService.class);

        // Maximum number of tokens/context size for the LLM
        private static final int DEFAULT_NUM_CTX = 13000;

        public void generateQuestions(List<DataModel> dataList) {

                OllamaOptions options = new OllamaOptions();
                options.withTemperature(0.0f).withNumCtx(DEFAULT_NUM_CTX);

                dataList.parallelStream().forEach(dataPiece -> {

                        ChatRequest request = ChatRequest.builder(ollamaConfig.getModel())
                                        .withMessages(getQuestionMessages(dataPiece))
                                        .withFormat("json").withOptions(options).build();

                        logger.info("AI Model: {}", ollamaConfig.getModel());

                        ChatResponse response = ollamaConfig.getOllamaApi().chat(request);

                        String content = response.message().content();

                        logger.info("llm's response {}, to the data {}", content, dataPiece);

                        OllamaThreeWordsResponseModel responseModel = parseJson(content);
                        if (responseModel == null) {
                                logger.warn("Json wasn't parsed for response: {}", content);
                                return;
                        }
                        String first = responseModel.getFirst_three_words();
                        String last = responseModel.getLast_three_words();
                        String question = responseModel.getQuestion();

                        if (first == null || last == null || question == null) {
                                return;
                        }

                        String fragment = TextUtil.parse(dataPiece.getContent(), first, last);

                        logger.info("Generated Question: {}, Found fragment: {}", question, fragment);

                        if (fragment == null) {
                                return;
                        }

                        dataPiece.setQuestion(question);
                        dataPiece.setAnswer(fragment);
                        return;
                });
        }

        private OllamaThreeWordsResponseModel parseJson(String content) {
                try {
                        OllamaThreeWordsResponseModel responseModel = objMapper.readValue(content,
                                        OllamaThreeWordsResponseModel.class);
                        return responseModel;
                } catch (JsonProcessingException e) {
                        logger.error("Error occurred while parsing json: {}, Response: {}", e.getMessage(),
                                        content);
                        return null;
                }
        }

        private List<Message> getQuestionMessages(DataModel dataPiece) {
                Message systemBiggerFragment = Message.builder(Role.SYSTEM)
                                .withContent(
                                                """
                                                                Act as an analyst who examines the text to identify and extract valuable fragments that fully explain ideas.
                                                                Ignore any text that doesn't encapsulate the idea fully. Ignore any text that doesn't contain a meaningful idea at all.
                                                                Avoid technical metadata or irrelevant content such as URLs, image captions, or locations. Only return content that adds value to understanding.
                                                                Be strict and don't hesitate to respond with \"null\" if no such fragment is present.
                                                                Provide a brief analysis of the text in the search for a valuable fragment (3 sentences).
                                                                If such fragment is found, provide first and last 3 words of the fragment to indicate where it can be found. The words must be exactly quoted from the text.
                                                                If such fragment found, provide an up-to-5-word question the fragment answers. Make the question related to the idea itself, not the text.
                                                                """)
                                .build();

                Message systemThreeWordsFormat = Message.builder(Role.SYSTEM)
                                .withContent("Respond in the following json format: {\"analysis\": \"A brief analysis of the text\", \"first_three_words\": \"First three words that indicate where the valuable fragment begins\", \"last_three_words\": \"Last three words that indicate where the valuable fragment ends\", \"question\": \"up-to-5-word question the fragment answers\"}")
                                .build();

                Message contextMessage = Message.builder(Role.USER)
                                .withContent(dataPiece.getContent())
                                .build();

                return List.of(systemBiggerFragment, systemThreeWordsFormat, contextMessage);
        }

        @Override
        public void generateAnswers(List<DataModel> data) {
                throw new UnsupportedOperationException("Unimplemented method 'generateAnswers'");
        }
}
