package com.tosan.client.s3.starter.configuration;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.Duration;

public class DurationSecondsSerializer extends ValueSerializer<Duration> {
    @Override
    public void serialize(Duration duration, JsonGenerator gen, SerializationContext ctx) {
        gen.writeString((duration.toMillis() / 1000.0) + "s");
    }

    public static SimpleModule getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Duration.class, new DurationSecondsSerializer());
        return module;
    }
}
