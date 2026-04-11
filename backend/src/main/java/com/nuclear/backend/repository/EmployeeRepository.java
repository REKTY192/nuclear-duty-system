package com.nuclear.backend.repository;

import com.nuclear.backend.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Только активные сотрудники
    List<Employee> findByIsActiveTrue();
}
