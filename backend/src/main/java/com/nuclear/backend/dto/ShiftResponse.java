package com.nuclear.backend.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ShiftResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private String shiftTypeName;
    private LocalDate shiftDate;
    private String status;
    private String notes;
}
