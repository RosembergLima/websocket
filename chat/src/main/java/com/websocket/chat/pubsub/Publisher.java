package com.websocket.chat.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.websocket.chat.config.RedisConfig;
import com.websocket.chat.data.User;
import com.websocket.chat.data.UserRepository;
import com.websocket.chat.dtos.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class Publisher {

    private final UserRepository userRepository;
    private final ReactiveStringRedisTemplate redisTemplate;

    @SneakyThrows
    public void publishChatMessage(String userIdFrom, String userIdTo, String text){
        User from = userRepository.findById(userIdFrom).orElseThrow();
        User to = userRepository.findById(userIdTo).orElseThrow();
        ChatMessage chatMessage = new ChatMessage(from, to, text);
        String chatMessageSerialized = new ObjectMapper().writeValueAsString(chatMessage);
        redisTemplate.convertAndSend(RedisConfig.CHAT_MESSAGES_CHANNEL, chatMessageSerialized).subscribe();
        log.info("[publishChatMessage] chat message was published");
    }
}
