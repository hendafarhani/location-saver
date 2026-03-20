package com.driver.location_saver.kafka.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracker.location_rider.model.Location;
import com.tracker.location_rider.model.RiderData;
import org.apache.kafka.common.errors.SerializationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiderDataJsonDeserializerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnNullWhenPayloadIsNull() {
        RiderData result;
        try (RiderDataJsonDeserializer deserializer = RiderDataJsonDeserializer.builder().objectMapper(objectMapper).build()) {

            result = deserializer.deserialize("topic", null);
        }

        assertNull(result);
        verifyNoInteractions(objectMapper);
    }

    @Test
    void shouldDeserializePayloadIntoRiderData() throws Exception {
        byte[] payload;
        RiderData expected;
        RiderData result;
        try (RiderDataJsonDeserializer deserializer = RiderDataJsonDeserializer.builder().objectMapper(objectMapper).build()) {
            payload = "{\"userName\":\"John\"}".getBytes();
            expected = RiderData.builder()
                    .identifier("rider-1")
                    .userName("John")
                    .location(Location.builder().latitude(1.0).longitude(2.0).radius(0.0).build())
                    .build();
            when(objectMapper.readValue(payload, RiderData.class)).thenReturn(expected);

            result = deserializer.deserialize("topic", payload);
        }

        assertSame(expected, result);
        verify(objectMapper).readValue(payload, RiderData.class);
    }

    @Test
    void shouldWrapIOExceptionInSerializationException() throws Exception {
        try (RiderDataJsonDeserializer deserializer = RiderDataJsonDeserializer.builder()
                .objectMapper(objectMapper)
                .build()) {
            byte[] payload = "invalid".getBytes();
            when(objectMapper.readValue(payload, RiderData.class)).thenThrow(new IOException("boom"));

            assertThrows(SerializationException.class, () -> deserializer.deserialize("topic", payload));
        }
    }
}
