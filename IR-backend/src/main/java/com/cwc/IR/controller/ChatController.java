package com.cwc.IR.controller;

import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@Slf4j
public class ChatController {

    private static final String API_SECRET_KEY = "174ad9004b0887939d2968af294769de.R2DrcsjUT6sZlMYo";
    private static final String requestIdTemplate = "cwc-chat";
    private static final ClientV4 client = new ClientV4.Builder(API_SECRET_KEY)
            .enableTokenCache()
            .networkConfig(300, 100, 100, 100, TimeUnit.SECONDS)
            .connectionPool(new okhttp3.ConnectionPool(8, 1, TimeUnit.SECONDS))
            .build();

    public static Flowable<ChatMessageAccumulator> mapStreamToAccumulator(Flowable<ModelData> flowable) {
        return flowable.map(chunk -> {
            return new ChatMessageAccumulator(chunk.getChoices().get(0).getDelta(), null, chunk.getChoices().get(0), chunk.getUsage(), chunk.getCreated(), chunk.getId());
        });
    }

    /**
     * 智谱 AI 问答 （流式）
     * @param question
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestParam String question) throws IOException {
        log.info("发送问题：{}", question);
        SseEmitter emitter = new SseEmitter();
        List<ChatMessage> messages = new ArrayList<>();
        // 请求参数 包含 role 和 question
        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), question);
        messages.add(chatMessage);
        String requestId = String.format(requestIdTemplate, System.currentTimeMillis());
        // 函数调用参数构建部分
        List<ChatTool> chatToolList = new ArrayList<>();
        ChatTool chatTool = new ChatTool();
        // 设置类型为 增加检索
        chatTool.setType(ChatToolType.RETRIEVAL.value());
        Retrieval retrieval = new Retrieval();
        // 知识库 ID
        retrieval.setKnowledge_id("1802687229408284672");
        retrieval.setPrompt_template("从文档\\n\\\"\\\"\\\"\\n{{knowledge}}\\n\\\"\\\"\\\"\\n中找关于\\n\\\"\\\"\\\"\\n{{question}}\\n\\\"\\\"\\\"\\n的信息，找到信息就仅使用文档语句回答问题，找不到答案就用自身知识回答并且告诉用户该信息不是来自文档。\\n不要复述问题，直接开始回答");
        chatTool.setRetrieval(retrieval);

        chatToolList.add(chatTool);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM3TURBO)
                .stream(Boolean.TRUE)
                .messages(messages)
                .requestId(requestId)
                .tools(chatToolList)
                .toolChoice("auto")
                .build();
        ModelApiResponse sseModelApiResp = client.invokeModelApi(chatCompletionRequest);

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            if (sseModelApiResp.isSuccess()) {
                AtomicBoolean isFirst = new AtomicBoolean(true);
                List<Choice> choices = new ArrayList<>();
                ChatMessageAccumulator chatMessageAccumulator = mapStreamToAccumulator(sseModelApiResp.getFlowable())
                        .doOnNext(accumulator -> {
                            {
                                if (accumulator.getDelta() != null && accumulator.getDelta().getContent() != null) {
                                    // 流式返回结果
                                    emitter.send(SseEmitter.event().data(accumulator.getDelta().getContent()));
                                }
                                choices.add(accumulator.getChoice());
                            }
                        })
                        .doOnComplete(() -> {   // 结束
                            emitter.send(SseEmitter.event().data("回答结束!"));
                            executor.shutdown();
                        })
                        .lastElement()
                        .blockingGet();
            }
        }, 0, 1, TimeUnit.SECONDS);

        return emitter;
    }
}
