package com.abhi.pulsar;

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

    public PulsarController(PulsarProducerService producerService) {
        this.producerService = producerService;
    }

    @PostMapping("/send")
    public void sendMessage(@RequestBody String message) throws PulsarClientException {
        log.info("Received request to send message: {}", message);
        producerService.sendMessage(message);
    }
}
