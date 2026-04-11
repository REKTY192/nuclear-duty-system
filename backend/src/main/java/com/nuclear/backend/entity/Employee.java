package com.nuclear.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "position")
    private String position;

    @Column(name = "clearance_level", nullable = false)
    private Integer clearanceLevel;

    @Column(name = "medical_expiry", nullable = false)
    private LocalDate medicalExpiry;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
