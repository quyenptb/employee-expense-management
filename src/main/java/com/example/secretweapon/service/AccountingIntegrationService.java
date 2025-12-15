package com.example.secretweapon.service;

import com.example.secretweapon.model.entity.ExpenseRequest;
import com.example.secretweapon.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountingIntegrationService {

    private final QuickBooksService quickBooksService;

    public void syncToAccountingSystem(ExpenseRequest request, User financeUser) {
        log.info(">>> START SYNCING TO QUICKBOOKS <<<");
        log.info("Request ID: {}", request.getRequestNo());

        try {
            quickBooksService.syncExpenseToQuickBooks(request);
            
            log.info(">>> SYNC SUCCESS: Data pushed to QuickBooks.");

        } catch (Exception e) {
            log.error(">>> SYNC FAILED: Could not push data to QuickBooks.", e);
            
        }
    }
}