package com.example.secretweapon.controller;

import com.example.secretweapon.exception.ResourceNotFoundException;
import com.example.secretweapon.model.enums.Currency;
import com.example.secretweapon.model.enums.ExpenseStatus;
import com.example.secretweapon.model.enums.ExpenseType;
import com.example.secretweapon.payload.request.ExpenseItemRequest;
import com.example.secretweapon.payload.request.ExpenseRequestCreate;
import com.example.secretweapon.payload.response.ExpenseRequestResponse;
import com.example.secretweapon.service.ExpenseService;
import com.example.secretweapon.service.JwtService;
import com.example.secretweapon.service.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpenseController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExpenseService expenseService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("createExpense_ValidRequest_shouldReturn201AndCreatedExpense")
    void createExpense_ValidRequest_shouldReturn201AndCreatedExpense() throws Exception {
        // Arrange
        ExpenseItemRequest itemRequest = new ExpenseItemRequest();
        itemRequest.setItemType(ExpenseType.MEALS);
        itemRequest.setAmount(new BigDecimal("50.00"));
        itemRequest.setDescription("Lunch");
        itemRequest.setIncurredDate(LocalDateTime.now());

        ExpenseRequestCreate request = new ExpenseRequestCreate();
        request.setTitle("Team Lunch");
        request.setAmount(new BigDecimal("100.00"));
        request.setProjectId(1L);
        request.setCurrency(Currency.USD);
        request.setItems(List.of(itemRequest));

        ExpenseRequestResponse response = new ExpenseRequestResponse();
        response.setId(1L);
        response.setTitle("Team Lunch");
        response.setStatus(ExpenseStatus.DRAFT);

        when(expenseService.createExpense(any(ExpenseRequestCreate.class), any())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Team Lunch"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    @DisplayName("createExpense_InvalidAmount_shouldReturn400")
    void createExpense_InvalidAmount_shouldReturn400() throws Exception {
        // Arrange
        ExpenseItemRequest itemRequest = new ExpenseItemRequest();
        itemRequest.setItemType(ExpenseType.MEALS);
        itemRequest.setAmount(new BigDecimal("50.00"));
        itemRequest.setIncurredDate(LocalDateTime.now());

        ExpenseRequestCreate request = new ExpenseRequestCreate();
        request.setTitle("Team Lunch");
        request.setAmount(new BigDecimal("-100.00"));
        request.setProjectId(1L);
        request.setCurrency(Currency.USD);
        request.setItems(List.of(itemRequest));

        // Act & Assert
        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getMyExpenses_AuthenticatedUser_shouldReturnListExpenses")
    void getMyExpenses_AuthenticatedUser_shouldReturnListExpenses() throws Exception {
        // Arrange
        ExpenseRequestResponse response = new ExpenseRequestResponse();
        response.setId(1L);
        response.setTitle("Business Trip");

        when(expenseService.getMyExpenses(any())).thenReturn(List.of(response));

        // Act & Assert
        mockMvc.perform(get("/api/expenses/my")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Business Trip"));
    }

    @Test
    @DisplayName("getExpenseById_ExistingId_shouldReturnExpense")
    void getExpenseById_ExistingId_shouldReturnExpense() throws Exception {
        // Arrange
        Long expenseId = 1L;
        ExpenseRequestResponse response = new ExpenseRequestResponse();
        response.setId(expenseId);

        when(expenseService.getExpenseById(eq(expenseId), any())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/expenses/{id}", expenseId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expenseId));
    }

    @Test
    @DisplayName("getExpenseById_NotFound_shouldReturn404")
    void getExpenseById_NotFound_shouldReturn404() throws Exception {
        // Arrange
        Long expenseId = 999L;
        when(expenseService.getExpenseById(eq(expenseId), any()))
                .thenThrow(new ResourceNotFoundException("Expense not found"));

        // Act & Assert
        mockMvc.perform(get("/api/expenses/{id}", expenseId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("updateExpense_ValidRequest_shouldReturnUpdatedExpense")
    void updateExpense_ValidRequest_shouldReturnUpdatedExpense() throws Exception {
        // Arrange
        Long expenseId = 1L;

        ExpenseItemRequest itemRequest = new ExpenseItemRequest();
        itemRequest.setItemType(ExpenseType.MEALS);
        itemRequest.setAmount(new BigDecimal("50.00"));
        itemRequest.setIncurredDate(LocalDateTime.now());

        ExpenseRequestCreate updateRequest = new ExpenseRequestCreate();
        updateRequest.setTitle("Updated Title");
        updateRequest.setAmount(new BigDecimal("200.00"));
        updateRequest.setProjectId(1L);
        updateRequest.setCurrency(Currency.VND);
        updateRequest.setItems(List.of(itemRequest));

        ExpenseRequestResponse response = new ExpenseRequestResponse();
        response.setId(expenseId);
        response.setTitle("Updated Title");

        when(expenseService.updateExpense(eq(expenseId), any(ExpenseRequestCreate.class), any()))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/expenses/{id}", expenseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    @DisplayName("deleteExpense_ExistingId_shouldReturn204")
    void deleteExpense_ExistingId_shouldReturn204() throws Exception {
        // Arrange
        Long expenseId = 1L;
        doNothing().when(expenseService).deleteExpense(eq(expenseId), any());

        // Act & Assert
        mockMvc.perform(delete("/api/expenses/{id}", expenseId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("submitExpense_ValidId_shouldReturnSubmittedExpense")
    void submitExpense_ValidId_shouldReturnSubmittedExpense() throws Exception {
        // Arrange
        Long expenseId = 1L;
        ExpenseRequestResponse response = new ExpenseRequestResponse();
        response.setId(expenseId);
        response.setStatus(ExpenseStatus.PENDING_MANAGER);

        when(expenseService.submitExpense(eq(expenseId), any())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/expenses/{id}/submit", expenseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_MANAGER"));
    }
}