package com.abhi.pulsar;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PulsarProducerService {

    private static final Logger log = LoggerFactory.getLogger(PulsarProducerService.class);
    private final Producer<byte[]> producer;
    private final Tracer tracer;

    public PulsarProducerService(PulsarClient pulsarClient, Tracer tracer) throws PulsarClientException {
        this.producer = pulsarClient.newProducer(Schema.BYTES)
                .topic("my-topic")
                .create();
        this.tracer = tracer;
    }

    public void sendMessage(String message) throws PulsarClientException {
        Span span = tracer.spanBuilder("pulsar.produce").startSpan();
        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("messaging.system", "pulsar");
            span.setAttribute("messaging.destination", "my-topic");
            log.info("Sending message: {}", message);
            producer.send(message.getBytes());
        } finally {
            span.end();
        }
    }
}
