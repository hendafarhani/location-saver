package com.driver.location_saver.mapper;


import com.driver.location_saver.redis.model.RiderDataRedis;
import com.tracker.location_rider.model.RiderData;


public class RiderMapper {

    public static RiderDataRedis mapRiderData(RiderData riderData){

        return RiderDataRedis.builder()
                .identifier(riderData.getIdentifier())
                .longitude(riderData.getLocation().getLongitude())
                .latitude(riderData.getLocation().getLatitude())
                .build();
    }
}
