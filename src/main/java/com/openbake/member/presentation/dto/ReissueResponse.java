package com.openbake.member.presentation.dto;

public record ReissueResponse(
        String accessToken,
        String refreshToken
) {}
