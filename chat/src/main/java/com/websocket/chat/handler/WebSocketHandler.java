package com.websocket.chat.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.websocket.chat.data.User;
import com.websocket.chat.dtos.ChatMessage;
import com.websocket.chat.events.Event;
import com.websocket.chat.events.EventType;
import com.websocket.chat.pubsub.Publisher;
import com.websocket.chat.services.TicketService;
import com.websocket.chat.services.UserService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Flow;

@Log4j2
@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private final TicketService ticketService;
    private final Publisher publisher;
    private final UserService userService;
    private final Map<String, WebSocketSession> sessions;
    private final Map<String, String> userIds;

    public WebSocketHandler(TicketService ticketService,
                            Publisher publisher,
                            UserService userService){
        this.ticketService = ticketService;
        this.publisher = publisher;
        this.userService = userService;
        this.sessions = new ConcurrentHashMap<>();
        this.userIds = new ConcurrentHashMap<>();
    }

    @SneakyThrows
    @Override
    public void afterConnectionEstablished(WebSocketSession session){
        log.info("[afterConnectionEstablished] session id: {}", session.getId());
        Optional<String> ticket = ticketOf(session);
        if(ticket.isEmpty() || ticket.get().isBlank()){
            log.warn("[afterConnectionEstablished] session {} without ticket", session.getId());
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        Optional<String> userId = ticketService.getUserIdByTicket(ticket.get());
        if(userId.isEmpty()){
          log.warn("[afterConnectionEstablished] session {} with invalid ticket", session.getId());
          session.close(CloseStatus.POLICY_VIOLATION);
          return;
        }

        sessions.put(userId.get(), session);
        userIds.put(session.getId(), userId.get());
        log.info("[afterConnectionEstablished] session {} was bind to user {}", session.getId(), userId.get());
        sendChatUsers(session);
    }

    private void sendChatUsers(WebSocketSession session) {
        List<User> chatUsers = userService.findChatUsers();
        Event<List<User>> event = new Event<>(EventType.CHAT_USERS_WERE_UPDATED, chatUsers);
        sendEvent(session, event);
    }
    @SneakyThrows
    private void sendEvent(WebSocketSession session, Event<?> event) {
        String eventSerialized = new ObjectMapper().writeValueAsString(event);
        session.sendMessage(new TextMessage(eventSerialized));
    }


    public void notify(ChatMessage chatMessage){
        Event<ChatMessage> event = new Event<>(EventType.CHAT_MESSAGE_WAS_CREATED, chatMessage);
        List<String> userIds = List.of(chatMessage.from().id(), chatMessage.to().id());
        userIds.stream()
                .distinct()
                .map(sessions::get)
                .filter(Objects::nonNull)
                .forEach(session -> sendEvent(session, event));
        log.info("[notify] chat message was notified");

    }
    private Optional<String> ticketOf(WebSocketSession session){
        return Optional.ofNullable(session.getUri())
                .map(UriComponentsBuilder::fromUri)
                .map(UriComponentsBuilder::build)
                .map(UriComponents::getQueryParams)
                .map(it -> it.get("ticket"))
                .flatMap(it -> it.stream().findFirst())
                .map(String::trim);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("[handleTextMessage] message: {} ", message.getPayload());
        if(message.getPayload().equals("ping")){
            session.sendMessage(new TextMessage("pong"));
            return;
        }
        MessagePayload payload = new ObjectMapper().readValue(message.getPayload(), MessagePayload.class);
        String userIdFrom = userIds.get(session.getId());
        publisher.publishChatMessage(userIdFrom, payload.to(), payload.text());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("[afterConnectionClosed] session id: {}", session.getId());
        String userId = userIds.get(session.getId());
        sessions.remove(userId);
        userIds.remove(session.getId());
    }
}
