package com.abhi.pulsar;

import com.abhi.pulsar.dto.User;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PulsarController {

    private static final Logger log = LoggerFactory.getLogger(PulsarController.class);
    private final PulsarProducerService producerService;
    private final Tracer tracer;

    public PulsarController(PulsarProducerService producerService, Tracer tracer) {
        this.producerService = producerService;
        this.tracer = tracer;
    }

    @PostMapping("/send")
    public void sendMessage(@RequestBody String message) throws PulsarClientException {
        log.info("Received request to send message: {}", message);
        producerService.sendMessage(message);
    }

    @PostMapping("/register")
    public void registerUser(@RequestBody User user) throws PulsarClientException {
        Span span = tracer.spanBuilder("registerUser").startSpan();
        try {
            log.info("Received request to register user: {}", user);
            producerService.sendUser(user);
            log.info("User registration request sent to Pulsar");
        } finally {
            span.end();
        }
    }
}
