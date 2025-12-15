package com.example.secretweapon.payload.request;

import com.example.secretweapon.model.enums.Currency;

import lombok.Data;

@Data
    public class ExpenseRequestUpdate {
        private String title;
        private String description;
        private Currency currency;
        private String attachments;
    }


    