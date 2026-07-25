package com.driver.location_saver.integration;

import com.driver.location_saver.service.ProcessDriverLocationService;
import com.tracker.location_rider.model.Location;
import com.tracker.location_rider.model.RiderData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false"
})
@Testcontainers(disabledWithoutDocker = true)
class ProcessDriverLocationRedisIT {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Autowired
    private ProcessDriverLocationService service;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @DynamicPropertySource
    static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @BeforeEach
    void cleanRedis() {
        stringRedisTemplate.delete(ProcessDriverLocationService.VEHICLE_LOCATION);
    }

    @Test
    void storesRiderDataInRedisGeoSet() {
        RiderData riderData = RiderData.builder()
                .identifier("rider-redis-1")
                .userName("Redis Rider")
                .location(Location.builder()
                        .latitude(48.8566)
                        .longitude(2.3522)
                        .radius(10)
                        .build())
                .build();

        service.storeDataInRedisCache(riderData);

        List<Point> positions = stringRedisTemplate.opsForGeo()
                .position(ProcessDriverLocationService.VEHICLE_LOCATION, "rider-redis-1");

        assertThat(positions).hasSize(1);
        assertThat(positions.getFirst().getX()).isCloseTo(2.3522, offset(0.0001));
        assertThat(positions.getFirst().getY()).isCloseTo(48.8566, offset(0.0001));
    }
}
