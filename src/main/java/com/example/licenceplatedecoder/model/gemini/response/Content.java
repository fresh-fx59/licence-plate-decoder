package com.example.licenceplatedecoder.model.gemini.response;

import java.util.List;

public record Content(List<Part> parts, String role) {}

