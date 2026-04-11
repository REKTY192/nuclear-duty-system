package com.nuclear.backend.repository;

import com.nuclear.backend.entity.ShiftType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftTypeRepository extends JpaRepository<ShiftType, Long> {
}
