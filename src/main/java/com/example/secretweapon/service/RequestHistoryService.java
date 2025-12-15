package com.example.secretweapon.service;


import com.example.secretweapon.model.entity.ExpenseRequest;
import com.example.secretweapon.model.entity.RequestHistory;
import com.example.secretweapon.model.entity.User;
import com.example.secretweapon.model.enums.HistoryAction;
import com.example.secretweapon.repository.RequestHistoryRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RequestHistoryService {

    private final RequestHistoryRepository historyRepository;

    @Transactional
    public void logHistory(ExpenseRequest request, User actor, HistoryAction action, String comment) {
        RequestHistory historyEntry = new RequestHistory(request, actor, action, comment);

    }
}