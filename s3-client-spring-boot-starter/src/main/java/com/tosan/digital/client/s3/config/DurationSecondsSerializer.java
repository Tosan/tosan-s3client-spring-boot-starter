package com.tosan.digital.client.s3.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.time.Duration;

public class DurationSecondsSerializer extends JsonSerializer<Duration> {
    @Override
    public void serialize(Duration duration, JsonGenerator gen, SerializerProvider prov) throws IOException {
        gen.writeString((duration.toMillis() / 1000.0) + "s");
    }

    public static SimpleModule getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Duration.class, new DurationSecondsSerializer());
        return module;
    }
}