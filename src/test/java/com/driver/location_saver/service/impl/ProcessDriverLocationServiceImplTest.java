package com.driver.location_saver.service.impl;

import com.driver.location_saver.kafka.handler.helper.DriverLocationHandlerTestHelper;
import com.tracker.location_rider.model.RiderData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.mockito.Mockito.*;


class ProcessDriverLocationServiceImplTest {

    StringRedisTemplate stringRedisTemplate;
    GeoOperations<String, String> geoOperations;
    ProcessDriverLocationServiceImpl processDriverLocationServiceMock;

    @Captor
    ArgumentCaptor<Point> pointArgumentCaptor;

    @Captor
    ArgumentCaptor<String> keyArgumentCaptor;

    @Captor
    ArgumentCaptor<String> memberArgumentCaptor;

    @BeforeEach
    void setUp() {
        stringRedisTemplate = mock(StringRedisTemplate.class);
        geoOperations = mock(GeoOperations.class);
        when(stringRedisTemplate.opsForGeo()).thenReturn(geoOperations);
        processDriverLocationServiceMock = new ProcessDriverLocationServiceImpl(stringRedisTemplate);
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void listen_Success() {
        RiderData riderDataTest = DriverLocationHandlerTestHelper.getRiderData();
        when(geoOperations.add(keyArgumentCaptor.capture(), pointArgumentCaptor.capture(), memberArgumentCaptor.capture())).thenReturn(1L);

        processDriverLocationServiceMock.storeDataInRedisCache(riderDataTest);

        Assertions.assertEquals("vehicle_location", keyArgumentCaptor.getValue());
        Assertions.assertEquals(riderDataTest.getLocation().getLongitude(), pointArgumentCaptor.getValue().getX());
        Assertions.assertEquals(riderDataTest.getLocation().getLatitude(), pointArgumentCaptor.getValue().getY());
        Assertions.assertEquals(riderDataTest.getIdentifier(), memberArgumentCaptor.getValue());
    }

}
