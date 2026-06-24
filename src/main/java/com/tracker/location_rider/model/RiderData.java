package com.tracker.location_rider.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RiderData implements Serializable {
    private String identifier;
    private String driverIdentifier;
    private String driverDisplayId;
    private String userName;
    private Location location;

}

