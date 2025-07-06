package com.driver.location_saver.redis.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiderDataRedis {
    private String identifier;
    private double latitude;
    private double longitude;
}
