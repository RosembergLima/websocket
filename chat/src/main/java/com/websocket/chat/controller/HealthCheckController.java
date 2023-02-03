package com.websocket.chat.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
public class HealthCheckController {

    @GetMapping
    void healthCheck(){log.info("[healthCheck] health check");}

}
