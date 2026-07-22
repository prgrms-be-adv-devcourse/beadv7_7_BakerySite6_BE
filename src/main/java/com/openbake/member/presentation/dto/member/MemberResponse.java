package com.openbake.member.presentation.dto.member;

import com.openbake.member.domain.Member;
import com.openbake.member.domain.MemberStatus;
import com.openbake.member.domain.Role;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

public record MemberResponse (
    Long id,
    String name,
    String email,
    String phoneNumber,
    Role role,
    MemberStatus status
) {
    public MemberResponse(Member member, String email) {
        this(member.getId(), member.getName(), email, member.getPhoneNumber(),
                member.getRole(), member.getStatus());
    }
}
