package com.example.licenceplatedecoder.serializer;

import com.example.licenceplatedecoder.model.gemini.response.Vehicle;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class VehicleListDeserializer extends JsonDeserializer<List<Vehicle>> {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<Vehicle> deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        String json = p.getValueAsString();
//        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        // Defensive: handle null, empty, or malformed JSON
        if (json == null || json.isBlank()) return List.of();
        try {
            Vehicle[] arr = mapper.readValue(json, Vehicle[].class);
            return Arrays.asList(arr);
        } catch (Exception e) {
            // Optionally log the error
            return List.of();
        }
    }
}
