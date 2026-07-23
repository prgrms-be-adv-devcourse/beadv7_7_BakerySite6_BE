package com.openbake.settlement.presentation;

import com.openbake.common.exception.GlobalExceptionHandler;
import com.openbake.member.infrastructure.jwt.JwtTokenProvider;
import com.openbake.settlement.application.MonthlySettlementBatchLauncher;
import com.openbake.settlement.application.MonthlySettlementBatchQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        MonthlySettlementBatchController.class,
        GlobalExceptionHandler.class
})
class MonthlySettlementBatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /** @RequiredArgsConstructor로 Controller가 생성되므로,
     * 생성자에 필요한 모든 의존성을 @MockitoBean으로 제공
     **/
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private MonthlySettlementBatchLauncher
            monthlySettlementBatchLauncher;

    @MockitoBean
    private MonthlySettlementBatchQueryService
            monthlySettlementBatchQueryService;

    @Test
    @DisplayName("periodStart가 누락되면 400 응답을 반환한다")
    void rejectMissingPeriodStart() throws Exception {
        String requestBody = """
                {
                  "periodEnd": "2026-08-01"
                }
                """;

        mockMvc.perform(
                        post("/internal/v1/settlement-batches/monthly")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.error.code")
                                .value("C001")
                )
                .andExpect(
                        jsonPath("$.error.message")
                                .value("periodStart는 필수입니다.")
                );

        verifyNoInteractions(monthlySettlementBatchLauncher);
    }

    @Test
    @DisplayName("periodEnd가 누락되면 400 응답을 반환한다")
    void rejectMissingPeriodEnd() throws Exception {
        String requestBody = """
                {
                  "periodStart": "2026-07-01"
                }
                """;

        mockMvc.perform(
                        post("/internal/v1/settlement-batches/monthly")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.error.code")
                                .value("C001")
                )
                .andExpect(
                        jsonPath("$.error.message")
                                .value("periodEnd는 필수입니다.")
                );

        verifyNoInteractions(monthlySettlementBatchLauncher);
    }

    @Test
    @DisplayName("날짜 형식이 올바르지 않으면 400 응답을 반환한다")
    void rejectInvalidDateFormat() throws Exception {
        String requestBody = """
                {
                  "periodStart": "2026/07/01",
                  "periodEnd": "2026-08-01"
                }
                """;

        mockMvc.perform(
                        post("/internal/v1/settlement-batches/monthly")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.error.code")
                                .value("C001")
                )
                .andExpect(
                        jsonPath("$.error.message")
                                .value("요청 형식이 올바르지 않습니다.")
                );

        verifyNoInteractions(monthlySettlementBatchLauncher);
    }

    @Test
    @DisplayName("요청 본문이 없으면 400 응답을 반환한다")
    void rejectMissingRequestBody() throws Exception {
        mockMvc.perform(
                        post("/internal/v1/settlement-batches/monthly")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.error.code")
                                .value("C001")
                );

        verifyNoInteractions(monthlySettlementBatchLauncher);
    }
}