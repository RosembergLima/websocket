package com.websocket.chat.providers;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.UrlJwkProvider;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.security.PublicKey;

@Component
public class JsonWebKeyProvider implements KeyProvider{

    private final UrlJwkProvider urlJwkProvider;

    @SneakyThrows
    public JsonWebKeyProvider(@Value("${app.auth.jwks-url}") final String jwkURL){
        this.urlJwkProvider = new UrlJwkProvider(new URL(jwkURL));
    }

    @Cacheable("public-key")
    @SneakyThrows
    @Override
    public PublicKey getPublicKey(String keyId) {
        final Jwk jwk = urlJwkProvider.get(keyId);
        return jwk.getPublicKey();
    }
}
