package com.driver.location_saver.integration;

import com.driver.location_saver.service.ProcessDriverLocationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracker.location_rider.model.Location;
import com.tracker.location_rider.model.RiderData;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false"
})
@Testcontainers(disabledWithoutDocker = true)
class RiderLocationKafkaTransferIntegrationTest {

    private static final String RIDER_IDENTIFIER = "rider-transfer-1";

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:latest"))
            .withExposedPorts(6379);

    @Autowired
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Value("${kafka.topics.rider-location}")
    private String riderLocationTopic;

    @Value("${kafka.listeners.rider-location.id}")
    private String riderLocationListenerId;

    @DynamicPropertySource
    static void registerContainerProperties(DynamicPropertyRegistry registry) {
        registry.add("kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }


    void cleanRedis() {
        stringRedisTemplate.delete(ProcessDriverLocationService.VEHICLE_LOCATION);
    }

    @BeforeEach
    void init(){
        cleanRedis();
        waitForListenerAssignment();
    }

    void waitForListenerAssignment() {
        // Avoid the auto.offset.reset=latest race: ensure the consumer has been
        // assigned its partition before any record is produced, otherwise the
        // record can be written before assignment and skipped past.
        MessageListenerContainer container =
                kafkaListenerEndpointRegistry.getListenerContainer(riderLocationListenerId);
        assert container != null;
        ContainerTestUtils.waitForAssignment(container, 1);
    }

    @Test
    void consumesRiderLocationFromKafkaAndStoresItInRedis() throws Exception {
        RiderData payload = RiderData.builder()
                .identifier(RIDER_IDENTIFIER)
                .userName("Transfer Rider")
                .location(Location.builder()
                        .latitude(48.8584)
                        .longitude(2.2945)
                        .radius(10)
                        .build())
                .build();

        kafkaTemplate.send(riderLocationTopic, RIDER_IDENTIFIER, objectMapper.writeValueAsBytes(payload))
                .get(10, TimeUnit.SECONDS);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<Point> positions = stringRedisTemplate.opsForGeo()
                    .position(ProcessDriverLocationService.VEHICLE_LOCATION, RIDER_IDENTIFIER);

            assertThat(positions).hasSize(1);
            Point storedPosition = positions.getFirst();
            assertThat(storedPosition).isNotNull();
            assertThat(storedPosition.getX()).isCloseTo(2.2945, withinGeoPrecision());
            assertThat(storedPosition.getY()).isCloseTo(48.8584, withinGeoPrecision());
        });
    }

    private org.assertj.core.data.Offset<Double> withinGeoPrecision() {
        return org.assertj.core.data.Offset.offset(0.0001);
    }

    @TestConfiguration
    static class KafkaProducerTestConfiguration {

        @Bean
        ProducerFactory<String, byte[]> testProducerFactory(
                @Value("${kafka.bootstrap-servers}") String bootstrapServers) {
            return new DefaultKafkaProducerFactory<>(Map.of(
                    ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class
            ));
        }

        @Bean
        KafkaTemplate<String, byte[]> testKafkaTemplate(ProducerFactory<String, byte[]> testProducerFactory) {
            return new KafkaTemplate<>(testProducerFactory);
        }
    }
}
