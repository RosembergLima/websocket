package com.websocket.chat.providers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class JsonWebTokenProvider implements TokenProvider{

    private final KeyProvider keyProvider;

    @Override
    public Map<String, String> decode(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);
        PublicKey publicKey = keyProvider.getPublicKey(decodedJWT.getKeyId());
        Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) publicKey, null);
        algorithm.verify(decodedJWT);
        boolean expired = decodedJWT
                .getExpiresAtAsInstant()
                .atZone(ZoneId.systemDefault())
                .isBefore(ZonedDateTime.now());
        if(expired) throw new RuntimeException("token is expired");
        return Map.of(
                "id", decodedJWT.getSubject(),
                "name", decodedJWT.getClaim("name").asString(),
                "picture", decodedJWT.getClaim("picture").asString()
        );
    }
}
