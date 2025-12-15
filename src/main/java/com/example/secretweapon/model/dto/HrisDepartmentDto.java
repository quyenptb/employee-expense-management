package com.example.secretweapon.model.dto;

import com.google.auto.value.AutoValue.Builder;

import lombok.Data;

@Data
@Builder
public class HrisDepartmentDto {
    private String externalId;
    private String name;
}