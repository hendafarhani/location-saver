package com.driver.location_saver.kafka.handler;

import com.driver.location_saver.kafka.handler.helper.DriverLocationHandlerTestHelper;
import com.driver.location_saver.service.ProcessDriverLocationService;
import com.tracker.location_rider.model.RiderData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class DriverLocationHandlerTest {


    ProcessDriverLocationService processDriverLocationServiceMock;
    SimpMessagingTemplate simpMessagingTemplateMock;

    DriverLocationHandler handler;

    @BeforeEach
    void setUp() {
        processDriverLocationServiceMock = mock(ProcessDriverLocationService.class);
        simpMessagingTemplateMock = mock(SimpMessagingTemplate.class);
        handler = new DriverLocationHandler(processDriverLocationServiceMock, simpMessagingTemplateMock);
    }

    @Test
    void listen_Success() { 
        RiderData riderDataTest = DriverLocationHandlerTestHelper.getRiderData();
        handler.listen(riderDataTest.getIdentifier(), riderDataTest);
        Mockito.verify(processDriverLocationServiceMock, times(1)).storeDataInRedisCache(riderDataTest);
        verify(simpMessagingTemplateMock).convertAndSend(DriverLocationHandler.DRIVER_POSITIONS_DESTINATION, riderDataTest.getIdentifier());
    }
}
