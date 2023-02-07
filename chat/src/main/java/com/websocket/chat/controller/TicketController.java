package com.websocket.chat.controller;

import com.websocket.chat.services.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("v1/ticket")
@CrossOrigin
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public Map<String, String> buildTicket(
            @RequestHeader(HttpHeaders.AUTHORIZATION)String authorization){
        String token = Optional.ofNullable(authorization)
                .map(it -> it.replace("Bearer ", ""))
                .orElse("");
        String ticket = ticketService.buildAndSaveTicket(token);
        return Map.of("ticket", ticket);
    }
}
