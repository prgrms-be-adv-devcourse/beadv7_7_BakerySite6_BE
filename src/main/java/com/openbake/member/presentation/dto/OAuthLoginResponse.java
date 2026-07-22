package com.openbake.member.presentation.dto;

public record OAuthLoginResponse(
        Long memberId,
        String email,
        String name,
        boolean newMember
) {}
