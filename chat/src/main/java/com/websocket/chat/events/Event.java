package com.websocket.chat.events;

public record Event<T>(EventType type, T payload) {
}
