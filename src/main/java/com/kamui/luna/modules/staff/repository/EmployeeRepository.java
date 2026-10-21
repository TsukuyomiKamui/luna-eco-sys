package com.kamui.luna.modules.staff.repository;

import com.kamui.luna.modules.staff.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // Поиск карточки HR по ID учетной записи пользователя (из токена авторизации)
    Optional<Employee> findByUserId(Long userId);
}