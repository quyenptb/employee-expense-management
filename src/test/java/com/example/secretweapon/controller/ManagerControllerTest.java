package com.example.secretweapon.controller;

import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.model.enums.DecisionType;
import com.example.secretweapon.model.enums.ExpenseStatus;
import com.example.secretweapon.payload.request.ApprovalRequest;
import com.example.secretweapon.payload.response.ExpenseRequestResponse;
import com.example.secretweapon.service.ManagerService;
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
class ManagerControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ManagerService managerService;

    @Mock
    private SlackNotificationService slackNotificationService;

    @InjectMocks
    private ManagerController managerController;

    @BeforeEach
    void setUp() {
        // Tự tạo ArgumentResolver để inject User vào @AuthenticationPrincipal
        HandlerMethodArgumentResolver putPrincipal = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().isAssignableFrom(User.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                User dummyManager = new User();
                dummyManager.setId(2L);
                dummyManager.setFullName("Test Manager");
                dummyManager.setEmail("manager@test.com");
                return dummyManager;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(managerController)
                .setCustomArgumentResolvers(putPrincipal)
                .build();
    }

    @Test
    @DisplayName("getTeamPendingRequests_shouldReturnList")
    void getTeamPendingRequests_shouldReturnList() throws Exception {
        // Arrange
        ExpenseRequestResponse res = new ExpenseRequestResponse();
        res.setId(1L);
        res.setStatus(ExpenseStatus.PENDING_MANAGER);

        when(managerService.getTeamPendingRequests(any())).thenReturn(List.of(res));

        // Act & Assert
        mockMvc.perform(get("/api/manager/requests/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @DisplayName("approveRequest_ValidId_shouldReturnApproved")
    void approveRequest_ValidId_shouldReturnApproved() throws Exception {
        // Arrange
        Long reqId = 1L;
        ApprovalRequest approvalRequest = new ApprovalRequest();
        approvalRequest.setComment("Approved");
        // FIX: Phải set Decision để tránh lỗi 400 Bad Request (Validation)
        approvalRequest.setDecision(DecisionType.APPROVED); 

        ExpenseRequestResponse res = new ExpenseRequestResponse();
        res.setId(reqId);
        res.setStatus(ExpenseStatus.PENDING_FINANCE);

        when(managerService.approveRequest(eq(reqId), any(), any(ApprovalRequest.class))).thenReturn(res);

        // Act & Assert
        mockMvc.perform(post("/api/manager/requests/{id}/approve", reqId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_FINANCE"));
    }

    @Test
    @DisplayName("rejectRequest_ValidId_shouldReturnRejected")
    void rejectRequest_ValidId_shouldReturnRejected() throws Exception {
        // Arrange
        Long reqId = 1L;
        ApprovalRequest approvalRequest = new ApprovalRequest();
        approvalRequest.setComment("Reject reason");
        // FIX: Phải set Decision
        approvalRequest.setDecision(DecisionType.REJECTED);

        ExpenseRequestResponse res = new ExpenseRequestResponse();
        res.setId(reqId);
        res.setStatus(ExpenseStatus.MANAGER_REJECTED);

        when(managerService.rejectRequest(eq(reqId), any(), any(ApprovalRequest.class))).thenReturn(res);

        // Act & Assert
        mockMvc.perform(post("/api/manager/requests/{id}/reject", reqId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MANAGER_REJECTED"));
    }
}