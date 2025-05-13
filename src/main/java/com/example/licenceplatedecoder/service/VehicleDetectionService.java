package com.example.licenceplatedecoder.service;

import com.example.licenceplatedecoder.model.MayBeVehicle;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

public interface VehicleDetectionService {
    Mono<MayBeVehicle> findVehicle(MultipartFile file);
} 