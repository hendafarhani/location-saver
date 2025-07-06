package com.driver.location_saver.kafka.handler;

import com.driver.location_saver.service.impl.ProcessDriverLocationServiceImpl;
import com.tracker.location_rider.model.RiderData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DriverLocationHandler {

    private final ProcessDriverLocationServiceImpl processDriverLocationService;

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(
            id = "riderLocationListener",
            topics = "rider.location",
            groupId = "driver.location.group",
            containerFactory = "riderLocationListenerFactory"
    )
    public void listen(RiderData riderData) {
        log.info("Received ride location from identifier: " + riderData.getIdentifier());
        processDriverLocationService.storeDataInRedisCache(riderData);
        log.info("Data saved in Redis under key: " + riderData.getIdentifier());
        // message should be JSON array of driver positions
        messagingTemplate.convertAndSend("/topic/driverPositions", riderData.getIdentifier());
    }

}
// This class listens to kafka messages on the "rider.location" topic, processes the received RiderData,
// stores it in Redis, and sends a message to a WebSocket topic for real-time updates.