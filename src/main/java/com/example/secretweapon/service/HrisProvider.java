package com.example.secretweapon.service;

import com.example.secretweapon.model.dto.*;


import java.util.List;

interface HrisProvider {
    String getProviderName(); // VD: "ORANGE_HRM", "ZOHO", "ODOO"
    List<HrisDepartmentDto> getDepartments();
    List<HrisUserDto> getEmployees();
}