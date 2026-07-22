package com.openbake.member.presentation;

import com.openbake.common.exception.DuplicateMemberException;
import com.openbake.common.exception.InvalidIdTokenException;
import com.openbake.member.application.AuthService;
import com.openbake.member.domain.AuthProvider;
import com.openbake.member.presentation.dto.OAuthLoginRequest;
import com.openbake.member.presentation.dto.OAuthLoginResponse;
import com.openbake.member.presentation.dto.SignupRequest;
import com.openbake.member.presentation.dto.SignupResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("회원가입 성공 시 200과 회원 정보를 반환한다")
    void signup_success() throws Exception {
        SignupRequest request = new SignupRequest("test@example.com", "password123", "홍길동", "010-1234-5678");
        given(authService.signup(any())).willReturn(new SignupResponse(1L, "test@example.com"));

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.memberId").value(1))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    @DisplayName("이미 가입된 이메일이면 409와 에러 응답을 반환한다")
    void signup_duplicateEmail() throws Exception {
        SignupRequest request = new SignupRequest("test@example.com", "password123", "홍길동", "010-1234-5678");
        given(authService.signup(any())).willThrow(new DuplicateMemberException());

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("C004"));
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 400과 에러 응답을 반환한다")
    void signup_invalidEmail() throws Exception {
        SignupRequest request = new SignupRequest("invalid-email", "password123", "홍길동", "010-1234-5678");

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("C001"));
    }

    @Test
    @DisplayName("필수 값이 비어있으면 400과 에러 응답을 반환한다")
    void signup_blankName() throws Exception {
        SignupRequest request = new SignupRequest("test@example.com", "password123", "", "010-1234-5678");

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("C001"));
    }

    @Test
    @DisplayName("Google 로그인 성공 시 200과 회원 정보를 반환한다")
    void loginWithOAuth_google_success() throws Exception {
        OAuthLoginRequest request = new OAuthLoginRequest("valid-id-token");
        given(authService.loginOrSignupWithOAuth(eq(AuthProvider.GOOGLE), any()))
                .willReturn(new OAuthLoginResponse(1L, "test@example.com", "홍길동", true));

        mockMvc.perform(post("/api/v1/auth/oauth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.memberId").value(1))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.newMember").value(true));
    }

    @Test
    @DisplayName("idToken이 비어있으면 400과 에러 응답을 반환한다")
    void loginWithOAuth_blankIdToken() throws Exception {
        OAuthLoginRequest request = new OAuthLoginRequest("");

        mockMvc.perform(post("/api/v1/auth/oauth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("C001"));
    }

    @Test
    @DisplayName("같은 이메일의 LOCAL 계정이 있으면 409와 에러 응답을 반환한다")
    void loginWithOAuth_duplicateLocalEmail() throws Exception {
        OAuthLoginRequest request = new OAuthLoginRequest("valid-id-token");
        given(authService.loginOrSignupWithOAuth(eq(AuthProvider.GOOGLE), any()))
                .willThrow(new DuplicateMemberException());

        mockMvc.perform(post("/api/v1/auth/oauth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("C004"));
    }

    @Test
    @DisplayName("ID 토큰 검증에 실패하면 401과 에러 응답을 반환한다")
    void loginWithOAuth_invalidToken() throws Exception {
        OAuthLoginRequest request = new OAuthLoginRequest("invalid-id-token");
        given(authService.loginOrSignupWithOAuth(eq(AuthProvider.GOOGLE), any()))
                .willThrow(new InvalidIdTokenException());

        mockMvc.perform(post("/api/v1/auth/oauth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("C005"));
    }

    @Test
    @DisplayName("지원하지 않는 provider 경로면 400과 에러 응답을 반환한다")
    void loginWithOAuth_unknownProvider() throws Exception {
        OAuthLoginRequest request = new OAuthLoginRequest("valid-id-token");

        mockMvc.perform(post("/api/v1/auth/oauth/facebook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("C001"));
    }
}
