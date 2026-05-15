package com.nuclear.backend.controller;

import com.nuclear.backend.dto.ShiftRequest;
import com.nuclear.backend.dto.ShiftResponse;
import com.nuclear.backend.service.ShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    @GetMapping
    public ResponseEntity<List<ShiftResponse>> getAllShifts() {
        return ResponseEntity.ok(shiftService.getAllShifts());
    }

    @GetMapping("/schedule")
    public ResponseEntity<List<ShiftResponse>> getSchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(shiftService.getShiftsByPeriod(from, to));
    }

    @PostMapping
    public ResponseEntity<ShiftResponse> createShift(@Valid @RequestBody ShiftRequest request) {
        return ResponseEntity.ok(shiftService.createShift(request));
    }

    // DELETE /api/shifts/{id} — удалить смену
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShift(@PathVariable Long id) {
        shiftService.deleteShift(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/shifts/{id}/status?status=COMPLETED — изменить статус
    @PatchMapping("/{id}/status")
    public ResponseEntity<ShiftResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(shiftService.updateShiftStatus(id, status));
    }
}
