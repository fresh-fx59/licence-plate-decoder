package com.example.licenceplatedecoder.model.gemini.response;

public record Candidate(Content content, String finishReason, double avgLogprobs) {}
