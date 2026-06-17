package com.driver.location_saver.service;

import com.tracker.location_rider.model.RiderData;

public interface ProcessDriverLocationService {

    String VEHICLE_LOCATION = "vehicle_location";

    void storeDataInRedisCache(RiderData riderData);

}
