package com.nuclear.backend.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class EmployeeResponse {

    private Long id;
    private String fullName;
    private String position;
    private Integer clearanceLevel;
    private LocalDate medicalExpiry;
    private Boolean isActive;

    // Вычисляемое поле — фронт использует его для подсветки
    private Boolean medicalExpiringSoon;
}
