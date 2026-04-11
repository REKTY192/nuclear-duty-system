package com.nuclear.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ShiftRequest {

    @NotNull(message = "ID сотрудника обязателен")
    private Long employeeId;

    @NotNull(message = "ID типа смены обязателен")
    private Long shiftTypeId;

    @NotNull(message = "Дата смены обязательна")
    private LocalDate shiftDate;

    private String notes;
}
