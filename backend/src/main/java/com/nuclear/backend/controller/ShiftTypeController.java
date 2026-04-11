package com.nuclear.backend.controller;

import com.nuclear.backend.entity.ShiftType;
import com.nuclear.backend.repository.ShiftTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shift-types")
@RequiredArgsConstructor
public class ShiftTypeController {

    private final ShiftTypeRepository shiftTypeRepository;

    // GET /api/shift-types — список всех типов смен
    @GetMapping
    public ResponseEntity<List<ShiftType>> getAllShiftTypes() {
        return ResponseEntity.ok(shiftTypeRepository.findAll());
    }
}
