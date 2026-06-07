package com.abhi.pulsar;

import com.abhi.pulsar.dto.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
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
    private final ObjectMapper objectMapper;

    public PulsarProducerService(PulsarClient pulsarClient, Tracer tracer, ObjectMapper objectMapper) throws PulsarClientException {
        this.producer = pulsarClient.newProducer(Schema.BYTES)
                .topic("user-registration")
                .create();
        this.tracer = tracer;
        this.objectMapper = objectMapper;
    }

    public void sendMessage(String message) throws PulsarClientException {
        Span span = tracer.spanBuilder("pulsar.producer").setSpanKind(SpanKind.PRODUCER).startSpan();
        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("messaging.system", "pulsar");
            span.setAttribute("messaging.destination", "my-topic");
            log.info("Sending message: {}", message);
            producer.send(message.getBytes());
        } finally {
            span.end();
        }
    }

    public void sendUser(User user) throws PulsarClientException {
        Span span = tracer.spanBuilder("pulsar.producer").setSpanKind(SpanKind.PRODUCER).startSpan();
        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("messaging.system", "pulsar");
            span.setAttribute("messaging.destination", "user-registration");
            log.info("Sending user: {}", user);
            byte[] bytes = objectMapper.writeValueAsBytes(user);
            producer.send(bytes);
        } catch (Exception e) {
            log.error("Error sending user", e);
        } finally {
            span.end();
        }
    }
}
