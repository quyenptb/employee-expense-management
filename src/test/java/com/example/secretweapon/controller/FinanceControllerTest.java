package com.example.secretweapon.controller;

import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.model.enums.DecisionType;
import com.example.secretweapon.model.enums.ExpenseStatus;
import com.example.secretweapon.payload.request.ApprovalRequest;
import com.example.secretweapon.payload.response.ExpenseRequestResponse;
import com.example.secretweapon.service.FinanceService;
import com.example.secretweapon.service.SlackNotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FinanceControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private FinanceService financeService;

    @Mock
    private SlackNotificationService slackNotificationService;

    @InjectMocks
    private FinanceController financeController;

    @BeforeEach
    void setUp() {
        // Mock User Injection for Finance Role
        HandlerMethodArgumentResolver putFinanceUser = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().isAssignableFrom(User.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                User finance = new User();
                finance.setId(3L);
                finance.setFullName("Ms. Accountant");
                return finance;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(financeController)
                .setCustomArgumentResolvers(putFinanceUser)
                .build();
    }

    @Test
    @DisplayName("getPendingFinanceRequests_shouldReturnList")
    void getPendingFinanceRequests_shouldReturnList() throws Exception {
        ExpenseRequestResponse res = new ExpenseRequestResponse();
        res.setId(10L);
        res.setStatus(ExpenseStatus.PENDING_FINANCE);

        when(financeService.getPendingFinanceRequests()).thenReturn(List.of(res));

        mockMvc.perform(get("/api/finance/requests/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L));
    }

    @Test
    @DisplayName("approveFinanceRequest_Valid_shouldReturnApproved")
    void approveFinanceRequest_Valid_shouldReturnApproved() throws Exception {
        Long reqId = 10L;
        ApprovalRequest request = new ApprovalRequest();
        request.setDecision(DecisionType.APPROVED);
        request.setComment("Budget OK");

        ExpenseRequestResponse res = new ExpenseRequestResponse();
        res.setId(reqId);
        res.setStatus(ExpenseStatus.APPROVED);

        when(financeService.approveFinanceRequest(eq(reqId), any(), any(ApprovalRequest.class)))
                .thenReturn(res);

        mockMvc.perform(post("/api/finance/requests/{id}/approve", reqId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("rejectFinanceRequest_Valid_shouldReturnRejected")
    void rejectFinanceRequest_Valid_shouldReturnRejected() throws Exception {
        Long reqId = 10L;
        ApprovalRequest request = new ApprovalRequest();
        request.setDecision(DecisionType.REJECTED);
        request.setComment("Over budget");

        ExpenseRequestResponse res = new ExpenseRequestResponse();
        res.setId(reqId);
        res.setStatus(ExpenseStatus.FINANCE_REJECTED);

        when(financeService.rejectFinanceRequest(eq(reqId), any(), any(ApprovalRequest.class)))
                .thenReturn(res);

        mockMvc.perform(post("/api/finance/requests/{id}/reject", reqId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINANCE_REJECTED"));
    }

    @Test
    @DisplayName("markAsPaid_Valid_shouldReturnPaid")
    void markAsPaid_Valid_shouldReturnPaid() throws Exception {
        Long reqId = 10L;
        ExpenseRequestResponse res = new ExpenseRequestResponse();
        res.setId(reqId);
        res.setStatus(ExpenseStatus.PAID);

        when(financeService.markAsPaid(eq(reqId), any())).thenReturn(res);

        mockMvc.perform(post("/api/finance/requests/{id}/pay", reqId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }
}