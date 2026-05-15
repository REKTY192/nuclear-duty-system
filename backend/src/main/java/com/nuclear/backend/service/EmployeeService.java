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

    public List<EmployeeResponse> getAllActiveEmployees() {
        return employeeRepository.findByIsActiveTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public EmployeeResponse createEmployee(EmployeeRequest request) {
        Employee employee = new Employee();
        employee.setFullName(request.getFullName());
        employee.setPosition(request.getPosition());
        employee.setClearanceLevel(request.getClearanceLevel());
        employee.setMedicalExpiry(request.getMedicalExpiry());
        employee.setIsActive(true);
        return toResponse(employeeRepository.save(employee));
    }

    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Сотрудник не найден"));
        employee.setFullName(request.getFullName());
        employee.setPosition(request.getPosition());
        employee.setClearanceLevel(request.getClearanceLevel());
        employee.setMedicalExpiry(request.getMedicalExpiry());
        return toResponse(employeeRepository.save(employee));
    }

    public void deactivateEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Сотрудник не найден"));
        employee.setIsActive(false);
        employeeRepository.save(employee);
    }

    private EmployeeResponse toResponse(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setFullName(employee.getFullName());
        response.setPosition(employee.getPosition());
        response.setClearanceLevel(employee.getClearanceLevel());
        response.setMedicalExpiry(employee.getMedicalExpiry());
        response.setIsActive(employee.getIsActive());

        LocalDate today = LocalDate.now();
        LocalDate expiry = employee.getMedicalExpiry();

        // Просрочен — дата уже прошла
        if (!expiry.isAfter(today)) {
            response.setMedicalExpiringSoon(true);
            response.setMedicalExpired(true);
        }
        // Истекает в течение 30 дней
        else if (expiry.isBefore(today.plusDays(30))) {
            response.setMedicalExpiringSoon(true);
            response.setMedicalExpired(false);
        } else {
            response.setMedicalExpiringSoon(false);
            response.setMedicalExpired(false);
        }

        return response;
    }
}
