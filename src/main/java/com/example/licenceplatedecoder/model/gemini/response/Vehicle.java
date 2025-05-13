package com.example.licenceplatedecoder.model.gemini.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Vehicle(
        @JsonProperty("isVehicle") boolean isVehicle,
        @JsonProperty("isEmergencyVehicle") boolean isEmergencyVehicle,
        @JsonProperty("vehicleNumber") String vehicleNumber
) {
    public Vehicle() {
        this(false, false, "ERROR");
    }
}
