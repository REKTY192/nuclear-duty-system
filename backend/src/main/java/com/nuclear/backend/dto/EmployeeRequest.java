package com.nuclear.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EmployeeRequest {

    @NotBlank(message = "ФИО обязательно")
    private String fullName;

    private String position;

    @NotNull(message = "Уровень допуска обязателен")
    @Min(value = 1, message = "Уровень допуска от 1 до 3")
    @Max(value = 3, message = "Уровень допуска от 1 до 3")
    private Integer clearanceLevel;

    @NotNull(message = "Дата медосмотра обязательна")
    private LocalDate medicalExpiry;
}
