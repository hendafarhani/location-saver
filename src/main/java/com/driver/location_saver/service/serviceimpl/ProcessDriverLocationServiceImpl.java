package com.driver.location_saver.service.serviceimpl;

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

    @Override
    public void storeDataInRedisCache(RiderData riderData) {
        RiderDataRedis riderDataRedis = RiderMapper.mapRiderData(riderData);
        storeRiderLocation(riderDataRedis);
    }

    private void storeRiderLocation(RiderDataRedis riderDataRedis) {
        stringRedisTemplate.opsForGeo().add(VEHICLE_LOCATION, toPoint(riderDataRedis), riderIdentifier(riderDataRedis));
    }

    private Point toPoint(RiderDataRedis riderDataRedis){
        return new Point(riderDataRedis.getLongitude(), riderDataRedis.getLatitude());
    }

    private String riderIdentifier(RiderDataRedis riderDataRedis) {
        if (riderDataRedis.getDriverIdentifier() != null && !riderDataRedis.getDriverIdentifier().isBlank()) {
            return riderDataRedis.getDriverIdentifier();
        }
        return riderDataRedis.getIdentifier();
    }
}
