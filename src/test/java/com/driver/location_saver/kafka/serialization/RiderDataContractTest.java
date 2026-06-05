package com.driver.location_saver.kafka.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracker.location_rider.model.Location;
import com.tracker.location_rider.model.RiderData;
import org.apache.kafka.common.errors.SerializationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiderDataContractTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void riderDataProducedByLocationRiderCanBeDeserializedByLocationSaver() throws Exception {
        RiderData producedByLocationRider = RiderData.builder()
                .identifier("rider-contract-1")
                .userName("Contract Rider")
                .location(Location.builder()
                        .latitude(48.8566)
                        .longitude(2.3522)
                        .radius(10)
                        .build())
                .build();
        byte[] payload = objectMapper.writeValueAsBytes(producedByLocationRider);

        RiderData consumedByLocationSaver;
        try (RiderDataJsonDeserializer deserializer = new RiderDataJsonDeserializer(objectMapper)) {
            consumedByLocationSaver = deserializer.deserialize("rider.location", payload);
        }

        assertThat(consumedByLocationSaver.getIdentifier()).isEqualTo("rider-contract-1");
        assertThat(consumedByLocationSaver.getUserName()).isEqualTo("Contract Rider");
        assertThat(consumedByLocationSaver.getLocation().getLatitude()).isEqualTo(48.8566);
        assertThat(consumedByLocationSaver.getLocation().getLongitude()).isEqualTo(2.3522);
        assertThat(consumedByLocationSaver.getLocation().getRadius()).isEqualTo(10);
    }

    @Test
    void invalidJsonFailsDeserialization() {
        try (RiderDataJsonDeserializer deserializer = new RiderDataJsonDeserializer(objectMapper)) {
            assertThatThrownBy(() -> deserializer.deserialize("rider.location", "{invalid-json".getBytes()))
                    .isInstanceOf(SerializationException.class)
                    .hasMessage("Failed to deserialize RiderData");
        }
    }
}
