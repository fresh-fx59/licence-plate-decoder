package com.example.licenceplatedecoder.service;

import com.example.licenceplatedecoder.model.MayBeVehicle;
import com.example.licenceplatedecoder.model.gemini.response.Vehicle;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;

public interface TestService {
    String getResponse() throws IOException;

    Vehicle findVehicle(MultipartFile file) throws IOException;

}
