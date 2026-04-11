package com.nuclear.backend.repository;

import com.nuclear.backend.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    // Все смены сотрудника за период (проверка 12 часов делается в Java)
    @Query("SELECT s FROM Shift s " +
           "WHERE s.employee.id = :employeeId " +
           "AND s.status = 'SCHEDULED' " +
           "AND s.shiftDate BETWEEN :from AND :to")
    List<Shift> findScheduledShiftsInDateRange(@Param("employeeId") Long employeeId,
                                               @Param("from") LocalDate from,
                                               @Param("to") LocalDate to);

    // Все смены сотрудника (для отображения)
    List<Shift> findByEmployeeIdOrderByShiftDateDesc(Long employeeId);
}
