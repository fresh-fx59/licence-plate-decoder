package com.example.licenceplatedecoder.service.impl;

import org.springframework.stereotype.Service;

@Service
public class VehicleDetectionGeminiServiceImpl {
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String GOOGLE_API_URL = "https://us-central1-aiplatform.googleapis.com/v1/projects/YOUR_PROJECT_ID/locations/us-central1/endpoints/YOUR_ENDPOINT_ID:predict";

    public Mono<MayBeVehicle> findVehicle(MultipartFile file) {
        Callable<MayBeVehicle> task = () -> {
            Path tempFile = Files.createTempFile(UUID.randomUUID().toString(), file.getOriginalFilename());
            try {
                Files.write(tempFile, file.getBytes());

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(GOOGLE_API_URL))
                        .header("Authorization", "Bearer " + getAccessToken())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(tempFile)))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                return parseResponse(response.body());
            } finally {
                Files.deleteIfExists(tempFile);
            }
        };

        Future<MayBeVehicle> future = executor.submit(task);

        return Mono.fromFuture(() -> {
            try {
                return future.get(10, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                throw new RuntimeException("Google AI processing timeout", e);
            } catch (InterruptedException | ExecutionException | IOException e) {
                throw new RuntimeException("Error during Google AI processing", e);
            }
        });
    }

    private String buildRequestBody(Path imagePath) throws IOException {
        String base64Image = java.util.Base64.getEncoder().encodeToString(Files.readAllBytes(imagePath));
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

    private MayBeVehicle parseResponse(String responseBody) {
        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonArray predictions = jsonObject.getAsJsonArray("predictions");
        if (predictions.size() == 0) return new MayBeVehicle(false, "", false);

        JsonObject prediction = predictions.get(0).getAsJsonObject();
        boolean isVehicle = prediction.has("vehicle") && prediction.get("vehicle").getAsBoolean();
        boolean isEmergencyVehicle = prediction.has("emergencyVehicle") && prediction.get("emergencyVehicle").getAsBoolean();
        String plateNumber = prediction.has("plateNumber") ? prediction.get("plateNumber").getAsString() : "";

        return new MayBeVehicle(isVehicle, plateNumber, isEmergencyVehicle);
    }
}
