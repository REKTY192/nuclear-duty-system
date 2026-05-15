package com.nuclear.backend.service;

import com.nuclear.backend.dto.EmployeeRequest;
import com.nuclear.backend.dto.EmployeeResponse;
import com.nuclear.backend.entity.Employee;
import com.nuclear.backend.exception.ValidationException;
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

    // Добавить сотрудника
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        Employee employee = new Employee();
        employee.setFullName(request.getFullName());
        employee.setPosition(request.getPosition());
        employee.setClearanceLevel(request.getClearanceLevel());
        employee.setMedicalExpiry(request.getMedicalExpiry());
        employee.setIsActive(true);
        return toResponse(employeeRepository.save(employee));
    }

    // Обновить данные сотрудника
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Сотрудник не найден"));
        employee.setFullName(request.getFullName());
        employee.setPosition(request.getPosition());
        employee.setClearanceLevel(request.getClearanceLevel());
        employee.setMedicalExpiry(request.getMedicalExpiry());
        return toResponse(employeeRepository.save(employee));
    }

    // Мягкое удаление — деактивация
    public void deactivateEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Сотрудник не найден"));
        employee.setIsActive(false);
        employeeRepository.save(employee);
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

        boolean expiringSoon = employee.getMedicalExpiry()
                .isBefore(LocalDate.now().plusDays(30));
        response.setMedicalExpiringSoon(expiringSoon);

        return response;
    }
}
