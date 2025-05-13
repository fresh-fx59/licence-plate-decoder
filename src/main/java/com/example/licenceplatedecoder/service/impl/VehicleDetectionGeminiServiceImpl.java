package com.example.licenceplatedecoder.service.impl;

import com.example.licenceplatedecoder.model.MayBeVehicle;
import com.example.licenceplatedecoder.service.VehicleDetectionService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Supplier;

@Service
public class VehicleDetectionGeminiServiceImpl implements VehicleDetectionService {
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String GOOGLE_API_URL = "https://us-central1-aiplatform.googleapis.com/v1/projects/YOUR_PROJECT_ID/locations/us-central1/endpoints/YOUR_ENDPOINT_ID:predict";

    public Mono<MayBeVehicle> findVehicle(MultipartFile file) {
        Supplier<MayBeVehicle> task = () -> {
            HttpRequest request = null;
            try {
                request = HttpRequest.newBuilder()
                        .uri(URI.create(GOOGLE_API_URL))
                        .header("Authorization", "Bearer " + getAccessToken())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(file.getBytes())))
                        .build();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            CompletableFuture<HttpResponse<String>> response = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
                return parseResponse(response);
        };

        CompletableFuture<MayBeVehicle> future = CompletableFuture.supplyAsync(task);

        return Mono.fromFuture((Supplier<CompletableFuture<MayBeVehicle>>) () -> future)
                .timeout(java.time.Duration.ofSeconds(10)) // Apply timeout directly to the Mono
                .onErrorResume(TimeoutException.class, e -> Mono.error(new RuntimeException("Google AI processing timeout", e)))
                .onErrorResume(InterruptedException.class, e -> Mono.error(new RuntimeException("Google AI processing interrupted", e)))
                .onErrorResume(ExecutionException.class, e -> Mono.error(new RuntimeException("Error during Google AI processing", e)))
                .flatMap(result -> result == null ? Mono.empty() : Mono.just(result));
    }

    private String buildRequestBody(byte[] imageBytes) throws IOException {
        String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);
        return "{" +
                "\"instances\":[{" +
                "\"content\":\"" + base64Image + "\"" +
                "}]" +
                "}";
    }

    private String getAccessToken() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
        credentials.refreshIfExpired();
        return credentials.getAccessToken().getTokenValue();
    }

    private MayBeVehicle parseResponse(CompletableFuture<HttpResponse<String>> responseBody) {
        JsonObject jsonObject;
        try {
            jsonObject = JsonParser.parseString(responseBody.get().body()).getAsJsonObject();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        JsonArray predictions = jsonObject.getAsJsonArray("predictions");
        if (predictions.isEmpty()) return new MayBeVehicle(false, "", false);

        JsonObject prediction = predictions.get(0).getAsJsonObject();
        boolean isVehicle = prediction.has("vehicle") && prediction.get("vehicle").getAsBoolean();
        boolean isEmergencyVehicle = prediction.has("emergencyVehicle") && prediction.get("emergencyVehicle").getAsBoolean();
        String plateNumber = prediction.has("plateNumber") ? prediction.get("plateNumber").getAsString() : "";

        return new MayBeVehicle(isVehicle, plateNumber, isEmergencyVehicle);
    }
}
