package com.example.licenceplatedecoder.model.gemini.response;

import com.example.licenceplatedecoder.serializer.VehicleListDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

public record Part(
        @JsonDeserialize(using = VehicleListDeserializer.class)
        @JsonProperty("text")
        List<Vehicle> vehicles
) {}
