package com.abhi.pulsar;

import com.abhi.pulsar.dto.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.annotation.PostConstruct;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PulsarConsumerService {

    private static final Logger log = LoggerFactory.getLogger(PulsarConsumerService.class);
    private final PulsarClient pulsarClient;
    private final Tracer tracer;
    private final ObjectMapper objectMapper;
    private Consumer<byte[]> consumer;

    public PulsarConsumerService(PulsarClient pulsarClient, Tracer tracer, ObjectMapper objectMapper) {
        this.pulsarClient = pulsarClient;
        this.tracer = tracer;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() throws PulsarClientException {
        consumer = pulsarClient.newConsumer()
                .topic("user-registration")
                .subscriptionName("user-registration-subscription")
                .messageListener(this::processMessage)
                .subscribe();
    }

    public void processMessage(Consumer<byte[]> consumer, Message<byte[]> msg) {
        Span span = tracer.spanBuilder("pulsar.consumer").setSpanKind(SpanKind.CONSUMER).startSpan();
        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("messaging.system", "pulsar");
            span.setAttribute("messaging.destination", "user-registration");
            User user = objectMapper.readValue(msg.getData(), User.class);
            log.info("Received user: {}", user);
            // Here you would typically save the user to a database
            log.info("Registered user: {}", user.getName());
            consumer.acknowledge(msg);
        } catch (Exception e) {
            log.error("Error processing message", e);
            consumer.negativeAcknowledge(msg);
        } finally {
            span.end();
        }
    }
}
