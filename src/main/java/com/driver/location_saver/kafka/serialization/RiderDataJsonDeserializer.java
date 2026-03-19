package com.driver.location_saver.kafka.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracker.location_rider.model.RiderData;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.Map;

/**
 * Dedicated deserializer to avoid deprecated Spring Kafka JSON serde classes.
 */
public class RiderDataJsonDeserializer implements Deserializer<RiderData> {

    private final ObjectMapper objectMapper;

    public RiderDataJsonDeserializer() {
        this(new ObjectMapper());
    }

    // Visible for testing
    RiderDataJsonDeserializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // no-op
    }

    @Override
    public RiderData deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.readValue(data, RiderData.class);
        } catch (IOException ex) {
            throw new SerializationException("Failed to deserialize RiderData", ex);
        }
    }

    @Override
    public void close() {
        // no resources to close
    }
}

