package com.abhi.pulsar;

import io.opentelemetry.api.trace.Span;
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
    private Consumer<byte[]> consumer;

    public PulsarConsumerService(PulsarClient pulsarClient, Tracer tracer) {
        this.pulsarClient = pulsarClient;
        this.tracer = tracer;
    }

    @PostConstruct
    public void init() throws PulsarClientException {
        consumer = pulsarClient.newConsumer()
                .topic("my-topic")
                .subscriptionName("my-subscription")
                .messageListener(this::processMessage)
                .subscribe();
    }

    public void processMessage(Consumer<byte[]> consumer, Message<byte[]> msg) {
        Span span = tracer.spanBuilder("pulsar.consume").startSpan();
        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("messaging.system", "pulsar");
            span.setAttribute("messaging.destination", "my-topic");
            String message = new String(msg.getData());
            log.info("Received message: {}", message);
            consumer.acknowledge(msg);
        } catch (Exception e) {
            log.error("Error processing message", e);
            consumer.negativeAcknowledge(msg);
        } finally {
            span.end();
        }
    }
}
