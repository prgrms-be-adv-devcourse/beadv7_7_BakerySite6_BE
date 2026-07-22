package com.openbake.member.application;

import com.openbake.common.exception.DuplicateMemberException;
import com.openbake.common.exception.EntityNotFoundException;
import com.openbake.member.domain.AuthCredential;
import com.openbake.member.domain.AuthProvider;
import com.openbake.member.domain.Member;
import com.openbake.member.infrastructure.AuthCredentialRepositoryImpl;
import com.openbake.member.infrastructure.MemberRepositoryImpl;
import com.openbake.member.infrastructure.oauth.OidcIdTokenVerifier;
import com.openbake.member.infrastructure.oauth.OidcIdentity;
import com.openbake.member.presentation.dto.OAuthLoginRequest;
import com.openbake.member.presentation.dto.OAuthLoginResponse;
import com.openbake.member.presentation.dto.SignupRequest;
import com.openbake.member.presentation.dto.SignupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepositoryImpl memberRepository;
    private final AuthCredentialRepositoryImpl authCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final OidcIdTokenVerifier oidcIdTokenVerifier;

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

    @Transactional
    public OAuthLoginResponse loginOrSignupWithOAuth(AuthProvider provider, OAuthLoginRequest request) {
        OidcIdentity identity = oidcIdTokenVerifier.verify(provider, request.idToken());

        Optional<AuthCredential> existing = authCredentialRepository
                .findByProviderAndProviderId(provider, identity.providerId());

        if (existing.isPresent()) {
            Member member = memberRepository.findById(existing.get().getMemberId())
                    .orElseThrow(() -> new EntityNotFoundException("연동된 회원 정보를 찾을 수 없습니다."));
            return new OAuthLoginResponse(member.getId(), identity.email(), member.getName(), false);
        }

        if (authCredentialRepository.existsByProviderAndEmail(AuthProvider.LOCAL, identity.email())) {
            throw new DuplicateMemberException();
        }

        Member savedMember = memberRepository.save(Member.createFromGoogle(identity.name()));
        authCredentialRepository.save(AuthCredential.createGoogle(
                savedMember.getId(), provider, identity.providerId(), identity.email()));

        return new OAuthLoginResponse(savedMember.getId(), identity.email(), savedMember.getName(), true);
    }

}
