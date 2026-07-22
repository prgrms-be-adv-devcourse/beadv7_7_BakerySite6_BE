package com.openbake.member.application;

import com.openbake.common.exception.DuplicateMemberException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepositoryImpl memberRepository;

    @Mock
    private AuthCredentialRepositoryImpl authCredentialRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OidcIdTokenVerifier oidcIdTokenVerifier;

    @InjectMocks
    private AuthService authService;

    private SignupRequest request;

    @BeforeEach
    void setUp() {
        request = new SignupRequest("test@example.com", "password123", "홍길동", "010-1234-5678");
    }

    @Test
    @DisplayName("회원가입에 성공하면 회원과 인증 정보를 저장하고 응답을 반환한다")
    void signup_success() {
        given(authCredentialRepository.existsByProviderAndEmail(AuthProvider.LOCAL, request.email()))
                .willReturn(false);

        Member savedMember = Member.create(request.name(), request.phoneNumber());
        ReflectionTestUtils.setField(savedMember, "id", 1L);
        given(memberRepository.save(any(Member.class))).willReturn(savedMember);

        given(passwordEncoder.encode(request.password())).willReturn("encodedPassword");
        given(authCredentialRepository.save(any(AuthCredential.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        SignupResponse response = authService.signup(request);

        assertThat(response.memberId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo(request.email());

        ArgumentCaptor<AuthCredential> captor = ArgumentCaptor.forClass(AuthCredential.class);
        verify(authCredentialRepository).save(captor.capture());
        AuthCredential savedCredential = captor.getValue();
        assertThat(savedCredential.getMemberId()).isEqualTo(1L);
        assertThat(savedCredential.getProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(savedCredential.getEmail()).isEqualTo(request.email());
        assertThat(savedCredential.getPasswordHash()).isEqualTo("encodedPassword");
    }

    @Test
    @DisplayName("이미 가입된 이메일이면 DuplicateMemberException을 던지고 저장하지 않는다")
    void signup_duplicateEmail_throwsException() {
        given(authCredentialRepository.existsByProviderAndEmail(AuthProvider.LOCAL, request.email()))
                .willReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(DuplicateMemberException.class);

        verify(memberRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
        verify(authCredentialRepository, never()).save(any());
    }

    @Test
    @DisplayName("신규 이메일로 Google 로그인하면 회원과 인증 정보를 새로 만들고 newMember=true를 반환한다")
    void loginOrSignupWithOAuth_newMember_createsAccount() {
        OAuthLoginRequest request = new OAuthLoginRequest("id-token");
        OidcIdentity identity = new OidcIdentity("google-sub-1", "new@example.com", "홍길동");
        given(oidcIdTokenVerifier.verify(AuthProvider.GOOGLE, "id-token")).willReturn(identity);

        given(authCredentialRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "google-sub-1"))
                .willReturn(Optional.empty());
        given(authCredentialRepository.existsByProviderAndEmail(AuthProvider.LOCAL, "new@example.com"))
                .willReturn(false);

        Member savedMember = Member.createFromGoogle("홍길동");
        ReflectionTestUtils.setField(savedMember, "id", 2L);
        given(memberRepository.save(any(Member.class))).willReturn(savedMember);
        given(authCredentialRepository.save(any(AuthCredential.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        OAuthLoginResponse response = authService.loginOrSignupWithOAuth(AuthProvider.GOOGLE, request);

        assertThat(response.memberId()).isEqualTo(2L);
        assertThat(response.email()).isEqualTo("new@example.com");
        assertThat(response.name()).isEqualTo("홍길동");
        assertThat(response.newMember()).isTrue();

        ArgumentCaptor<AuthCredential> captor = ArgumentCaptor.forClass(AuthCredential.class);
        verify(authCredentialRepository).save(captor.capture());
        AuthCredential savedCredential = captor.getValue();
        assertThat(savedCredential.getMemberId()).isEqualTo(2L);
        assertThat(savedCredential.getProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(savedCredential.getProviderId()).isEqualTo("google-sub-1");
        assertThat(savedCredential.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    @DisplayName("이미 GOOGLE로 가입된 사용자가 다시 로그인하면 새로 만들지 않고 기존 회원 정보를 반환한다")
    void loginOrSignupWithOAuth_existingMember_returnsLogin() {
        OAuthLoginRequest request = new OAuthLoginRequest("id-token");
        OidcIdentity identity = new OidcIdentity("google-sub-2", "exist@example.com", "기존회원");
        given(oidcIdTokenVerifier.verify(AuthProvider.GOOGLE, "id-token")).willReturn(identity);

        AuthCredential existingCredential = AuthCredential.createGoogle(
                5L, AuthProvider.GOOGLE, "google-sub-2", "exist@example.com");
        given(authCredentialRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "google-sub-2"))
                .willReturn(Optional.of(existingCredential));

        Member existingMember = Member.create("기존회원", "010-0000-0000");
        ReflectionTestUtils.setField(existingMember, "id", 5L);
        given(memberRepository.findById(5L)).willReturn(Optional.of(existingMember));

        OAuthLoginResponse response = authService.loginOrSignupWithOAuth(AuthProvider.GOOGLE, request);

        assertThat(response.memberId()).isEqualTo(5L);
        assertThat(response.name()).isEqualTo("기존회원");
        assertThat(response.newMember()).isFalse();

        verify(memberRepository, never()).save(any());
        verify(authCredentialRepository, never()).save(any());
    }

    @Test
    @DisplayName("같은 이메일의 LOCAL 계정이 이미 있으면 DuplicateMemberException을 던지고 저장하지 않는다")
    void loginOrSignupWithOAuth_localEmailConflict_throwsException() {
        OAuthLoginRequest request = new OAuthLoginRequest("id-token");
        OidcIdentity identity = new OidcIdentity("google-sub-3", "local@example.com", "이름");
        given(oidcIdTokenVerifier.verify(AuthProvider.GOOGLE, "id-token")).willReturn(identity);

        given(authCredentialRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "google-sub-3"))
                .willReturn(Optional.empty());
        given(authCredentialRepository.existsByProviderAndEmail(AuthProvider.LOCAL, "local@example.com"))
                .willReturn(true);

        assertThatThrownBy(() -> authService.loginOrSignupWithOAuth(AuthProvider.GOOGLE, request))
                .isInstanceOf(DuplicateMemberException.class);

        verify(memberRepository, never()).save(any());
        verify(authCredentialRepository, never()).save(any());
    }
}
