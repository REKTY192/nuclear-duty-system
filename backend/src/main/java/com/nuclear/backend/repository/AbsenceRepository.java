package com.nuclear.backend.repository;

import com.nuclear.backend.entity.Absence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AbsenceRepository extends JpaRepository<Absence, Long> {

    // Проверка: есть ли у сотрудника отсутствие в указанную дату
    @Query("SELECT COUNT(a) > 0 FROM Absence a " +
           "WHERE a.employee.id = :employeeId " +
           "AND :date BETWEEN a.startDate AND a.endDate")
    boolean existsByEmployeeIdAndDate(@Param("employeeId") Long employeeId,
                                      @Param("date") LocalDate date);
}
