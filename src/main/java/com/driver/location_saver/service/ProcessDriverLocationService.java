package com.driver.location_saver.service;

import com.tracker.location_rider.model.RiderData;

public interface ProcessDriverLocationService {

    void storeDataInRedisCache(RiderData riderData);

}
