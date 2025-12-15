package com.example.secretweapon.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZohoEmployeeDto {
    
    @JsonProperty("recordId")
    private String recordId;

    @JsonProperty("Employee ID")
    private String employeeId;

    @JsonProperty("First Name")
    private String firstName;

    @JsonProperty("Last Name")
    private String lastName;

    @JsonProperty("Email address")
    private String email;

    @JsonProperty("Employee Status")
    private String status; // Active, Terminated...

    @JsonProperty("Zoho Role")
    private String zohoRole;
    

    // @JsonProperty("Department") 
    // private String department;

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
}