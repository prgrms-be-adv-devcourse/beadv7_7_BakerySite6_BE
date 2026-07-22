package com.openbake.member.presentation.dto;

public record OAuthLoginResponse(
        Long memberId,
        String accessToken,
        String refreshToken,
        String email,
        String name,
        boolean newMember
) {}
