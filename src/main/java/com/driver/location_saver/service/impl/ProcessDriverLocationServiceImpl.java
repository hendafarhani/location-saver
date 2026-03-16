package com.driver.location_saver.service.impl;

import com.driver.location_saver.mapper.RiderMapper;
import com.driver.location_saver.redis.model.RiderDataRedis;
import com.driver.location_saver.service.ProcessDriverLocationService;
import com.tracker.location_rider.model.RiderData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProcessDriverLocationServiceImpl implements ProcessDriverLocationService {

    private final StringRedisTemplate stringRedisTemplate;
    public static final String vehicleLocation = "vehicle_location";

    @Override
    public void storeDataInRedisCache(RiderData riderData) {
        RiderDataRedis riderDataRedis = RiderMapper.mapRiderData(riderData);

        stringRedisTemplate.opsForGeo().add(vehicleLocation, getPoint(riderDataRedis), riderDataRedis.getIdentifier());
    }

    private Point getPoint(RiderDataRedis riderDataRedis){
        return new Point(riderDataRedis.getLongitude(), riderDataRedis.getLatitude());
    }
}
