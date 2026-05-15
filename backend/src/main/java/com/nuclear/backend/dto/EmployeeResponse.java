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

    // true — медосмотр истекает в течение 30 дней или уже просрочен
    private Boolean medicalExpiringSoon;

    // true — медосмотр уже просрочен (дата в прошлом)
    private Boolean medicalExpired;
}
