package com.driver.location_saver.kafka.handler;

import com.driver.location_saver.service.ProcessDriverLocationService;
import com.tracker.location_rider.model.RiderData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DriverLocationHandler {

    public static final String DRIVER_POSITIONS_DESTINATION = "/topic/driverPositions";

    private final ProcessDriverLocationService processDriverLocationService;

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(
            id = "${kafka.listeners.rider-location.id}",
            topics = "${kafka.topics.rider-location}",
            groupId = "${kafka.consumers.rider-location.group-id}",
            containerFactory = "riderLocationListenerFactory"
    )
    public void listen(@Header(KafkaHeaders.RECEIVED_KEY) String riderIdentifier, RiderData riderData) {
        logReceivedLocation(riderIdentifier, riderData);
        saveRiderLocation(riderData);
        publishDriverPosition(riderData);
    }

    private void logReceivedLocation(String riderIdentifier, RiderData riderData) {
        log.info("Received ride location from Kafka key {} and payload identifier {}",
                riderIdentifier,
                riderData.getIdentifier());
    }

    private void saveRiderLocation(RiderData riderData) {
        processDriverLocationService.storeDataInRedisCache(riderData);
        log.info("Data saved in Redis under key: {}", riderData.getIdentifier());
    }

    private void publishDriverPosition(RiderData riderData) {
        messagingTemplate.convertAndSend(DRIVER_POSITIONS_DESTINATION, riderData.getIdentifier());
    }
}
