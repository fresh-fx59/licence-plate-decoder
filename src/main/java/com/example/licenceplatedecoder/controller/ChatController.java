package com.example.licenceplatedecoder.controller;

import com.example.licenceplatedecoder.model.MayBeVehicle;
import com.example.licenceplatedecoder.model.gemini.response.Vehicle;
import com.example.licenceplatedecoder.service.TestService;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

@RestController
public class ChatController {

    private final VertexAiGeminiChatModel chatModel;
    private final TestService testService;

    @Autowired
    public ChatController(VertexAiGeminiChatModel chatModel, TestService testService) {
        this.chatModel = chatModel;
        this.testService = testService;
    }

    @GetMapping("/ai/generate")
    public Map<?, ?> generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return Map.of("generation", this.chatModel.call(message));
    }

    @GetMapping("/ai/generateStream")
    public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return this.chatModel.stream(prompt);
    }

    @GetMapping("/ai/generateGemini")
    public String generateGeminiApi() throws IOException {
        return testService.getResponse();
    }

    @PostMapping(value = "/ai/generateGemini", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Vehicle> generateGeminiApiPost(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.getSize() > 3 * 1024 * 1024) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Vehicle());
        }
        if (!Set.of("image/jpeg", "image/png", "image/jpg").contains(file.getContentType())) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(new Vehicle());
        }
        return ResponseEntity.ok(testService.findVehicle(file));
    }
}
