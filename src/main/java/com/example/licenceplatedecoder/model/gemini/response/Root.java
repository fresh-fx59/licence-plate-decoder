package com.example.licenceplatedecoder.model.gemini.response;

import java.util.List;

public record Root(List<Candidate> candidates, UsageMetadata usageMetadata, String modelVersion) {}
