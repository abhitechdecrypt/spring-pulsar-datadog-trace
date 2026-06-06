package com.abhi.pulsar;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TracerProvider {

    @Bean
    public Tracer tracer() {
        return GlobalOpenTelemetry.getTracer("pulsar-instrumentation");
    }
}
