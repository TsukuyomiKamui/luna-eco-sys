package com.kamui.luna.modules.staff.repository;

import com.kamui.luna.modules.staff.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    // Поиск активной смены конкретного сотрудника
    Optional<Shift> findByEmployeeIdAndStatus(Long employeeId, String status);
}