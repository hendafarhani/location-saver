package com.driver.location_saver.kafka.handler;

import com.driver.location_saver.kafka.handler.helper.DriverLocationHandlerTestHelper;
import com.driver.location_saver.service.impl.ProcessDriverLocationServiceImpl;
import com.tracker.location_rider.model.RiderData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

class DriverLocationHandlerTest {


    ProcessDriverLocationServiceImpl processDriverLocationServiceMock;
    SimpMessagingTemplate simpMessagingTemplateMock;

    DriverLocationHandler handler;

    @BeforeEach
    void setUp() {
        processDriverLocationServiceMock = mock(ProcessDriverLocationServiceImpl.class);
        simpMessagingTemplateMock = mock(SimpMessagingTemplate.class);
        handler = new DriverLocationHandler(processDriverLocationServiceMock, simpMessagingTemplateMock);
    }

    @Test
    void listen_Success() { 
        RiderData riderDataTest = DriverLocationHandlerTestHelper.getRiderData();
        handler.listen(riderDataTest);
        Mockito.verify(processDriverLocationServiceMock, times(1)).storeDataInRedisCache(riderDataTest);
    }
}