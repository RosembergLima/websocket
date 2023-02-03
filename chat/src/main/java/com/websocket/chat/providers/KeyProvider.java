package com.websocket.chat.providers;

import java.security.PublicKey;

public interface KeyProvider {
    PublicKey getPublicKey(String keyId);
}
