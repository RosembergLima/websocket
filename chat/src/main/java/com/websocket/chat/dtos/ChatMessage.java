package com.websocket.chat.dtos;

import com.websocket.chat.data.User;

public record ChatMessage(User from, User to, String text) {
}
