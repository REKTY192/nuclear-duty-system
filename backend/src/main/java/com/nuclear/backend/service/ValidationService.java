package com.nuclear.backend.service;

import com.nuclear.backend.entity.Employee;
import com.nuclear.backend.entity.ShiftType;
import com.nuclear.backend.entity.Shift;
import com.nuclear.backend.exception.ValidationException;
import com.nuclear.backend.repository.AbsenceRepository;
import com.nuclear.backend.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ValidationService {

    private final AbsenceRepository absenceRepository;
    private final ShiftRepository shiftRepository;

    /**
     * Главный метод валидации — вызывается перед сохранением любой смены.
     * Проверяет все 4 правила последовательно.
     */
    public void validateShift(Employee employee, ShiftType shiftType, LocalDate shiftDate) {

        checkEmployeeActive(employee);
        checkMedicalExpiry(employee, shiftDate);
        checkAbsence(employee, shiftDate);
        checkTwelveHourRule(employee, shiftType, shiftDate);
    }

    // ─── Правило 1: Сотрудник должен быть активен ───────────────────────────

    private void checkEmployeeActive(Employee employee) {
        if (!employee.getIsActive()) {
            throw new ValidationException(
                    "Сотрудник «" + employee.getFullName() + "» отстранён от работы и не может быть назначен на дежурство."
            );
        }
    }

    // ─── Правило 2: Медосмотр должен быть действителен ──────────────────────

    private void checkMedicalExpiry(Employee employee, LocalDate shiftDate) {
        if (!shiftDate.isBefore(employee.getMedicalExpiry())) {
            throw new ValidationException(
                    "Медосмотр сотрудника «" + employee.getFullName() + "» истекает " +
                    employee.getMedicalExpiry() + ". Назначение на " + shiftDate + " невозможно."
            );
        }
    }

    // ─── Правило 3: Сотрудник не должен отсутствовать в этот день ───────────

    private void checkAbsence(Employee employee, LocalDate shiftDate) {
        boolean isAbsent = absenceRepository.existsByEmployeeIdAndDate(employee.getId(), shiftDate);
        if (isAbsent) {
            throw new ValidationException(
                    "Сотрудник «" + employee.getFullName() + "» отсутствует " + shiftDate +
                    " (отпуск, больничный или командировка)."
            );
        }
    }

    // ─── Правило 4: Правило 12 часов ─────────────────────────────────────────
    // Получаем смены за ±2 дня от новой даты, затем проверяем в Java.

    private void checkTwelveHourRule(Employee employee, ShiftType shiftType, LocalDate shiftDate) {
        LocalDateTime newShiftStart = shiftDate.atTime(shiftType.getStartTime());

        // Берём смены за 2 дня до и 1 день после — с запасом
        List<Shift> nearbyShifts = shiftRepository.findScheduledShiftsInDateRange(
                employee.getId(),
                shiftDate.minusDays(2),
                shiftDate.plusDays(1)
        );

        for (Shift existing : nearbyShifts) {
            LocalDateTime existingStart = existing.getShiftDate()
                    .atTime(existing.getShiftType().getStartTime());
            LocalDateTime existingEnd = existingStart
                    .plusHours(existing.getShiftType().getDurationHours());

            // Проверяем: новая смена начинается раньше чем через 12ч после конца существующей
            boolean tooSoonAfter = newShiftStart.isAfter(existingEnd) &&
                    newShiftStart.isBefore(existingEnd.plusHours(12));

            // Или существующая смена начинается раньше чем через 12ч после конца новой
            LocalDateTime newShiftEnd = newShiftStart.plusHours(shiftType.getDurationHours());
            boolean tooSoonBefore = existingStart.isAfter(newShiftEnd) &&
                    existingStart.isBefore(newShiftEnd.plusHours(12));

            if (tooSoonAfter || tooSoonBefore) {
                throw new ValidationException(
                        "Нарушение правила 12 часов: сотрудник «" + employee.getFullName() +
                        "» уже имеет смену " + existing.getShiftDate() +
                        ". Между сменами должно быть не менее 12 часов."
                );
            }
        }
    }
}
