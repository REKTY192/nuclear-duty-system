package com.nuclear.backend.service;

import com.nuclear.backend.dto.EmployeeResponse;
import com.nuclear.backend.entity.Employee;
import com.nuclear.backend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    // Получить всех активных сотрудников
    public List<EmployeeResponse> getAllActiveEmployees() {
        return employeeRepository.findByIsActiveTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Маппинг Entity → DTO
    private EmployeeResponse toResponse(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setFullName(employee.getFullName());
        response.setPosition(employee.getPosition());
        response.setClearanceLevel(employee.getClearanceLevel());
        response.setMedicalExpiry(employee.getMedicalExpiry());
        response.setIsActive(employee.getIsActive());

        // Медосмотр истекает в течение 30 дней — фронт подсветит красным
        boolean expiringSoon = employee.getMedicalExpiry()
                .isBefore(LocalDate.now().plusDays(30));
        response.setMedicalExpiringSoon(expiringSoon);

        return response;
    }
}
