package com.openbake.member.application;

import com.openbake.common.exception.DuplicateMemberException;
import com.openbake.member.domain.AuthCredential;
import com.openbake.member.domain.AuthProvider;
import com.openbake.member.domain.Member;
import com.openbake.member.infrastructure.AuthCredentialRepositoryImpl;
import com.openbake.member.infrastructure.MemberRepositoryImpl;
import com.openbake.member.presentation.dto.SignupRequest;
import com.openbake.member.presentation.dto.SignupResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepositoryImpl memberRepository;
    private final AuthCredentialRepositoryImpl authCredentialRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (authCredentialRepository.existsByProviderAndEmail(AuthProvider.LOCAL, request.email())) {
            throw new DuplicateMemberException();
        }

        Member member = Member.create(request.name(), request.phoneNumber());
        Member savedMember = memberRepository.save(member);

        String encodedPassword = passwordEncoder.encode(request.password());
        AuthCredential authCredential = AuthCredential.createLocal(
                savedMember.getId(), request.email(), encodedPassword);
        authCredentialRepository.save(authCredential);

        return new SignupResponse(savedMember.getId(), request.email());
    }


}
