package com.example.licenceplatedecoder.model.gemini.response;

import java.util.List;

public record UsageMetadata(
        int promptTokenCount,
        int candidatesTokenCount,
        int totalTokenCount,
        List<TokensDetail> promptTokensDetails,
        List<TokensDetail> candidatesTokensDetails
) {}
