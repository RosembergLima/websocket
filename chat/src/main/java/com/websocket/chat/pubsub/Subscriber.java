package com.websocket.chat.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.websocket.chat.config.RedisConfig;
import com.websocket.chat.dtos.ChatMessage;
import com.websocket.chat.handler.WebSocketHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class Subscriber {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final WebSocketHandler webSocketHandler;

    @PostConstruct
    private void init(){
        this.redisTemplate
                .listenTo(ChannelTopic.of(RedisConfig.CHAT_MESSAGES_CHANNEL))
                .map(ReactiveSubscription.Message::getMessage)
                .subscribe(this::onChatMessage);
    }

    @SneakyThrows
    private void onChatMessage(final String chatMessageSerialized) {
        log.info("[onChatMessage] chat message was received");
        ChatMessage chatMessage = new ObjectMapper().readValue(chatMessageSerialized, ChatMessage.class);
        webSocketHandler.notify(chatMessage);
    }
}
