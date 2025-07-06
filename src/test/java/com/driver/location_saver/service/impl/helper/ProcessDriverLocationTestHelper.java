package com.driver.location_saver.service.impl.helper;

import com.tracker.location_rider.model.RiderData;
import com.tracker.location_rider.model.Location;

public class ProcessDriverLocationTestHelper {

    public static RiderData getRiderData(){
        return RiderData.builder()
                .identifier("326462-test")
                .location(Location.builder()
                .longitude(465464)
                .latitude(6542345).build())
                .build();
    }

}
