package com.openbake.member.presentation.dto;

import com.openbake.member.domain.MemberStatus;
import com.openbake.member.domain.Role;

public record LocalLoginResponse (
    Long memberId,
    Role role
) {}
