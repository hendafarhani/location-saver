package com.driver.location_saver.service.impl;

import com.driver.location_saver.mapper.RiderMapper;
import com.driver.location_saver.redis.model.RiderDataRedis;
import com.driver.location_saver.service.ProcessDriverLocationService;
import com.tracker.location_rider.model.RiderData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProcessDriverLocationServiceImpl implements ProcessDriverLocationService {

    private final GeoOperations<String, String> geoOperations;
    public static final String vehicleLocation = "vehicle_location";

    @Override
    public void storeDataInRedisCache(RiderData riderData) {
        RiderDataRedis riderDataRedis = RiderMapper.mapRiderData(riderData);

        geoOperations.add(vehicleLocation, getPoint(riderDataRedis), riderDataRedis.getIdentifier());
    }

    private Point getPoint(RiderDataRedis riderDataRedis){
        return new Point(riderDataRedis.getLongitude(), riderDataRedis.getLatitude());
    }
}
