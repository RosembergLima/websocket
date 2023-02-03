package com.websocket.chat.data;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collation = "user")
public record User(String id, String name, String picture) {
}
