package com.example.licenceplatedecoder.service.impl;

import com.example.licenceplatedecoder.model.gemini.response.Root;
import com.example.licenceplatedecoder.model.gemini.response.Vehicle;
import com.example.licenceplatedecoder.service.TestService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

@Service
public class TestServiceImpl implements TestService {
    private static final Logger log = Logger.getLogger(TestServiceImpl.class.getName());

    @Value("google.api.key")
    private String GOOGLE_API_KEY;
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    //        String responseString = "{  \"candidates\": [    {      \"content\": {        \"parts\": [          {            \"text\": \"[\\n  {\\n    \\\"isVehicle\\\": true,\\n    \\\"isEmergencyVehicle\\\": true,\\n    \\\"vehicleNumber\\\": \\\"C941YE777\\\"\\n  }\\n]\"          }        ],        \"role\": \"model\"      },      \"finishReason\": \"STOP\",      \"avgLogprobs\": -2.1621820494709981e-05    }  ],  \"usageMetadata\": {    \"promptTokenCount\": 1884,    \"candidatesTokenCount\": 42,    \"totalTokenCount\": 1926,    \"promptTokensDetails\": [      {        \"modality\": \"TEXT\",        \"tokenCount\": 78      },      {        \"modality\": \"IMAGE\",        \"tokenCount\": 1806      }    ],    \"candidatesTokensDetails\": [      {        \"modality\": \"TEXT\",        \"tokenCount\": 42      }    ]  },  \"modelVersion\": \"gemini-2.0-flash\"}";

    @Override
    public Vehicle findVehicle(MultipartFile file) throws IOException {
        byte[] imageBytes = file.getBytes();

        List<Vehicle> vehicles = getVehicles(imageBytes);

        return vehicles.isEmpty() ? null : vehicles.getFirst();
    }

    @Override
    public String getResponse() throws IOException {
        // Set your variables
        String imgPath = "/Users/a/Downloads/some-car.jpg"; // <-- set your image path
            // <-- set your API key

        // 1. Read image and encode in Base64
        byte[] imageBytes = Files.readAllBytes(Paths.get(imgPath));

        List<Vehicle> vehicles = getVehicles(imageBytes);


        return vehicles.isEmpty() ? "null" : vehicles.getFirst().toString();
    }

    private List<Vehicle> getVehicles(byte[] imageBytes) throws IOException {
        // 2. Construct JSON payload
        JSONObject payload = preparePayload(imageBytes);

        // 3. Send HTTP POST request
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + GOOGLE_API_KEY;
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 4. Read and print the response
        int status = conn.getResponseCode();
        InputStream responseStream = (status < 400) ? conn.getInputStream() : conn.getErrorStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(responseStream));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        String responseString = response.toString();


        // 5.

        //try1
        List<Vehicle> vehicles = new ArrayList<>();
        try {
            Root root = MAPPER.readValue(responseString, Root.class);
            vehicles.addAll(root.candidates().getFirst().content().parts().getFirst().vehicles());
        } catch (Exception e) {
            log.log(SEVERE, String.format("Error parsing JSON \\n %s \\n %s", responseString, e.getMessage()), e);
        }

        return vehicles;
    }

    private JSONObject preparePayload(byte[] imageBytes) {
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        JSONObject inlineData = new JSONObject()
                .put("mime_type", "image/jpeg")
                .put("data", base64Image);

        JSONArray parts = new JSONArray()
                .put(new JSONObject().put("text",
                        "Tell me is there a car or truck on an image. Put this answer in isVehicle field. " +
                                "Tell me the licence plate number without spaces. Put it into vehicleNumber field." +
                                "Tell me if it is an ambulance, police or fire truck. If yes, set isEmergencyVehicle to true."))
                .put(new JSONObject().put("inline_data", inlineData));

        JSONArray contents = new JSONArray()
                .put(new JSONObject().put("parts", parts));

        JSONObject generationConfig = new JSONObject();

        generationConfig.put("responseMimeType", "application/json");

        JSONObject responseSchema = new JSONObject();
        responseSchema.put("type", "ARRAY");

        JSONObject items = new JSONObject();
        items.put("type", "OBJECT");

        JSONObject properties = new JSONObject();

        JSONObject vehicleNumber = new JSONObject();
        vehicleNumber.put("type", "STRING");
        properties.put("vehicleNumber", vehicleNumber);

        JSONObject isVehicle = new JSONObject();
        isVehicle.put("type", "BOOLEAN");
        properties.put("isVehicle", isVehicle);

        JSONObject isEmergencyVehicle = new JSONObject();
        isEmergencyVehicle.put("type", "BOOLEAN");
        properties.put("isEmergencyVehicle", isEmergencyVehicle);

        items.put("properties", properties);
        items.put("propertyOrdering", new JSONArray(Arrays.asList("isVehicle", "isEmergencyVehicle", "vehicleNumber")));

        responseSchema.put("items", items);

        generationConfig.put("responseSchema", responseSchema);

        return new JSONObject()
                .put("contents", contents)
                .put("generationConfig", generationConfig)
                ;
    }
}
