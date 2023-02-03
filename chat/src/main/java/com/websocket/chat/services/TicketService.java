package com.websocket.chat.services;

import com.websocket.chat.data.User;
import com.websocket.chat.data.UserRepository;
import com.websocket.chat.providers.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final RedisTemplate<String, String> redisTemplate;

    private final TokenProvider tokenProvider;

    private final UserRepository userRepository;

    public String buildAndSaveTicket(String token){
        if(token == null || token.isBlank()) throw new RuntimeException("Missing token");
        String ticket = UUID.randomUUID().toString();
        Map<String, String> user = tokenProvider.decode(token);
        String userId = user.get("id");
        redisTemplate.opsForValue().set(ticket, userId, Duration.ofSeconds(10L));
        saveUser(user);
        return ticket;
    }

    private void saveUser(Map<String, String> user){
        userRepository.save(new User(user.get("id"), user.get("name"), user.get("picture")));
    }

    public Optional<String> getUserIdByTicket(String ticket){
        return Optional.ofNullable(redisTemplate.opsForValue().getAndDelete(ticket));
    }
}
