package com.driver.location_saver.mapper;

import com.driver.location_saver.redis.model.RiderDataRedis;
import com.tracker.location_rider.model.Location;
import com.tracker.location_rider.model.RiderData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiderMapperTest {

    @Mock
    private RiderData riderData;

    @Mock
    private Location location;

    @Test
    void shouldMapAllFieldsFromRiderDataToRiderDataRedis() {
        when(riderData.getIdentifier()).thenReturn("rider-123");
        when(riderData.getLocation()).thenReturn(location);
        when(location.getLongitude()).thenReturn(77.5946);
        when(location.getLatitude()).thenReturn(12.9716);

        RiderDataRedis mapped = RiderMapper.mapRiderData(riderData);

        assertNotNull(mapped);
        assertEquals("rider-123", mapped.getIdentifier());
        assertEquals(77.5946, mapped.getLongitude());
        assertEquals(12.9716, mapped.getLatitude());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenRiderDataIsNull() throws NoSuchMethodException {
        Method mapMethod = RiderMapper.class.getMethod("mapRiderData", RiderData.class);

        InvocationTargetException thrown = assertThrows(
                InvocationTargetException.class,
                () -> mapMethod.invoke(null, new Object[]{null})
        );

        assertInstanceOf(NullPointerException.class, thrown.getCause());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenLocationIsNull() {
        when(riderData.getLocation()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> RiderMapper.mapRiderData(riderData));
    }
}
