package com.example.licenceplatedecoder.controller;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.licenceplatedecoder.model.MayBeVehicle;
import com.example.licenceplatedecoder.service.VehicleDetectionService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/vehicle")
public class VehicleDetectionController {
    private final VehicleDetectionService detectionService;

    VehicleDetectionController(VehicleDetectionService detectionService) {
        this.detectionService = detectionService;
    }

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<MayBeVehicle>> analyzeImage(@RequestParam("file") MultipartFile file) {
        if (file.getSize() > 3 * 1024 * 1024) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null));
        }
        if (!Set.of("image/jpeg", "image/png", "image/jpg").contains(file.getContentType())) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(null));
        }
        return detectionService.findVehicle(file).map(ResponseEntity::ok);
    }
}
