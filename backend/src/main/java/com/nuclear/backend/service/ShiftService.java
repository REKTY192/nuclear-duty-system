package com.nuclear.backend.service;

import com.nuclear.backend.dto.ShiftRequest;
import com.nuclear.backend.dto.ShiftResponse;
import com.nuclear.backend.entity.Employee;
import com.nuclear.backend.entity.Shift;
import com.nuclear.backend.entity.ShiftType;
import com.nuclear.backend.exception.ValidationException;
import com.nuclear.backend.repository.EmployeeRepository;
import com.nuclear.backend.repository.ShiftRepository;
import com.nuclear.backend.repository.ShiftTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftTypeRepository shiftTypeRepository;
    private final ValidationService validationService;

    // Назначить смену
    public ShiftResponse createShift(ShiftRequest request) {

        // Находим сотрудника и тип смены
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ValidationException("Сотрудник не найден"));

        ShiftType shiftType = shiftTypeRepository.findById(request.getShiftTypeId())
                .orElseThrow(() -> new ValidationException("Тип смены не найден"));

        // ← Весь «мозг» системы — 4 проверки
        validationService.validateShift(employee, shiftType, request.getShiftDate());

        // Валидация пройдена — сохраняем
        Shift shift = new Shift();
        shift.setEmployee(employee);
        shift.setShiftType(shiftType);
        shift.setShiftDate(request.getShiftDate());
        shift.setNotes(request.getNotes());
        shift.setStatus("SCHEDULED");

        Shift saved = shiftRepository.save(shift);
        return toResponse(saved);
    }

    // Получить все смены
    public List<ShiftResponse> getAllShifts() {
        return shiftRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Маппинг Entity → DTO
    private ShiftResponse toResponse(Shift shift) {
        ShiftResponse response = new ShiftResponse();
        response.setId(shift.getId());
        response.setEmployeeId(shift.getEmployee().getId());
        response.setEmployeeName(shift.getEmployee().getFullName());
        response.setShiftTypeName(shift.getShiftType().getName());
        response.setShiftDate(shift.getShiftDate());
        response.setStatus(shift.getStatus());
        response.setNotes(shift.getNotes());
        return response;
    }
}
