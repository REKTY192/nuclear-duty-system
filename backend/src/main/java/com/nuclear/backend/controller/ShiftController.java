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

    // GET /api/shifts — список всех смен
    @GetMapping
    public ResponseEntity<List<ShiftResponse>> getAllShifts() {
        return ResponseEntity.ok(shiftService.getAllShifts());
    }

    // GET /api/shifts/schedule?from=2026-04-01&to=2026-04-30 — график за период
    @GetMapping("/schedule")
    public ResponseEntity<List<ShiftResponse>> getSchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(shiftService.getShiftsByPeriod(from, to));
    }

    // POST /api/shifts — назначить смену (с валидацией)
    @PostMapping
    public ResponseEntity<ShiftResponse> createShift(@Valid @RequestBody ShiftRequest request) {
        ShiftResponse response = shiftService.createShift(request);
        return ResponseEntity.ok(response);
    }
}
