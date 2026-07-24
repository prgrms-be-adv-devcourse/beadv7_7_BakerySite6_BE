package com.openbake.seller.presentation;

import com.openbake.common.exception.AccountVerificationExpiredException;
import com.openbake.common.exception.AccountVerificationFailedException;
import com.openbake.common.exception.BusinessVerificationFailedException;
import com.openbake.common.exception.EntityNotFoundException;
import com.openbake.common.exception.InvalidSettlementAccountException;
import com.openbake.member.domain.AccessTokenRepository;
import com.openbake.member.infrastructure.jwt.JwtTokenProvider;
import com.openbake.seller.application.SellerService;
import com.openbake.seller.presentation.dto.AccountVerificationConfirmRequest;
import com.openbake.seller.presentation.dto.AccountVerificationConfirmResponse;
import com.openbake.seller.presentation.dto.AccountVerificationStartRequest;
import com.openbake.seller.presentation.dto.AccountVerificationStartResponse;
import com.openbake.seller.presentation.dto.BusinessVerificationRequest;
import com.openbake.seller.presentation.dto.BusinessVerificationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SellerController.class)
@AutoConfigureMockMvc(addFilters = false)
class SellerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SellerService sellerService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private AccessTokenRepository accessTokenRepository;

    @Test
    @DisplayName("사업자 인증 성공 시 200을 반환한다")
    void verifyBusiness_success() throws Exception {
        BusinessVerificationRequest request =
                new BusinessVerificationRequest("123-45-67890", "서울시", "이세종");
        given(sellerService.verifyBusiness(any()))
                .willReturn(new BusinessVerificationResponse(true, "123-45-67890", LocalDateTime.now()));

        mockMvc.perform(post("/api/v1/sellers/business-verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.verified").value(true));
    }

    @Test
    @DisplayName("사업자등록번호 형식이 올바르지 않으면 400과 C001을 반환한다")
    void verifyBusiness_invalidFormat() throws Exception {
        BusinessVerificationRequest request =
                new BusinessVerificationRequest("invalid", "서울시", "이세종");

        mockMvc.perform(post("/api/v1/sellers/business-verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("C001"));
    }

    @Test
    @DisplayName("등록된 사업자 정보와 불일치하면 400과 SE001을 반환한다")
    void verifyBusiness_mismatch() throws Exception {
        BusinessVerificationRequest request =
                new BusinessVerificationRequest("123-45-67890", "서울시", "홍길동");
        willThrow(new BusinessVerificationFailedException()).given(sellerService).verifyBusiness(any());

        mockMvc.perform(post("/api/v1/sellers/business-verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("SE001"));
    }

    @Test
    @DisplayName("계좌 인증 요청 성공 시 200을 반환한다")
    void requestAccountVerification_success() throws Exception {
        AccountVerificationStartRequest request =
                new AccountVerificationStartRequest("088", "1101234567", "이세종");
        given(sellerService.requestAccountVerification(any()))
                .willReturn(new AccountVerificationStartResponse("vr_1", LocalDateTime.now().plusMinutes(3)));

        mockMvc.perform(post("/api/v1/sellers/settlement-account/verification-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationRequestId").value("vr_1"));
    }

    @Test
    @DisplayName("등록되지 않은 은행 코드면 400과 SE002를 반환한다")
    void requestAccountVerification_invalidBankCode() throws Exception {
        AccountVerificationStartRequest request =
                new AccountVerificationStartRequest("999", "1101234567", "이세종");
        willThrow(new InvalidSettlementAccountException())
                .given(sellerService).requestAccountVerification(any());

        mockMvc.perform(post("/api/v1/sellers/settlement-account/verification-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("SE002"));
    }

    @Test
    @DisplayName("계좌번호 형식이 올바르지 않으면 400과 C001을 반환한다")
    void requestAccountVerification_invalidAccountNumberFormat() throws Exception {
        AccountVerificationStartRequest request =
                new AccountVerificationStartRequest("088", "not-a-number", "이세종");

        mockMvc.perform(post("/api/v1/sellers/settlement-account/verification-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("C001"));
    }

    @Test
    @DisplayName("계좌 인증 확인 성공 시 200을 반환한다")
    void verifyAccount_success() throws Exception {
        AccountVerificationConfirmRequest request = new AccountVerificationConfirmRequest("1234");
        given(sellerService.verifyAccount(eq("vr_1"), any()))
                .willReturn(new AccountVerificationConfirmResponse(true, LocalDateTime.now()));

        mockMvc.perform(post("/api/v1/sellers/settlement-account/verification-requests/vr_1/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verified").value(true));
    }

    @Test
    @DisplayName("인증 코드가 일치하지 않으면 400과 SE003을 반환한다")
    void verifyAccount_codeMismatch() throws Exception {
        AccountVerificationConfirmRequest request = new AccountVerificationConfirmRequest("9999");
        willThrow(new AccountVerificationFailedException())
                .given(sellerService).verifyAccount(eq("vr_1"), any());

        mockMvc.perform(post("/api/v1/sellers/settlement-account/verification-requests/vr_1/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("SE003"));
    }

    @Test
    @DisplayName("인증 유효 시간이 만료되면 410과 SE004를 반환한다")
    void verifyAccount_expired() throws Exception {
        AccountVerificationConfirmRequest request = new AccountVerificationConfirmRequest("1234");
        willThrow(new AccountVerificationExpiredException())
                .given(sellerService).verifyAccount(eq("vr_1"), any());

        mockMvc.perform(post("/api/v1/sellers/settlement-account/verification-requests/vr_1/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.error.code").value("SE004"));
    }

    @Test
    @DisplayName("존재하지 않는 인증 요청이면 404와 C003을 반환한다")
    void verifyAccount_notFound() throws Exception {
        AccountVerificationConfirmRequest request = new AccountVerificationConfirmRequest("1234");
        willThrow(new EntityNotFoundException("대상을 찾을 수 없습니다."))
                .given(sellerService).verifyAccount(eq("vr_1"), any());

        mockMvc.perform(post("/api/v1/sellers/settlement-account/verification-requests/vr_1/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("C003"));
    }
}
