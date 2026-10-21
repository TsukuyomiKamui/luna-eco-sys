package com.kamui.luna.core.config;

import com.kamui.luna.modules.auth.entity.Role;
import com.kamui.luna.modules.auth.entity.User;
import com.kamui.luna.modules.auth.repository.RoleRepository;
import com.kamui.luna.modules.auth.repository.UserRepository;
import com.kamui.luna.modules.staff.entity.Employee;
import com.kamui.luna.modules.staff.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "app.bootstrap.enabled", havingValue = "true", matchIfMissing = true)
public class BootstrapDataConfig implements ApplicationRunner {
    private final UserRepository users;
    private final RoleRepository roles;
    private final EmployeeRepository employees;
    private final PasswordEncoder encoder;
    @Value("${app.bootstrap.admin-username}") private String username;
    @Value("${app.bootstrap.admin-password}") private String password;

    public BootstrapDataConfig(UserRepository users, RoleRepository roles, EmployeeRepository employees,
                               PasswordEncoder encoder) {
        this.users = users; this.roles = roles; this.employees = employees; this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Role admin = roles.findByName("ADMIN").orElseThrow();
        User user = users.findByUsername(username).orElseGet(() -> {
            User created = new User();
            created.setUsername(username);
            created.setPasswordHash(encoder.encode(password));
            created.setActive(true);
            created.setRoles(Set.of(admin));
            return users.save(created);
        });
        if (employees.findByUserId(user.getId()).isEmpty()) {
            Employee employee = new Employee();
            employee.setUserId(user.getId());
            employee.setFirstName("Luna");
            employee.setLastName("Administrator");
            employee.setPosition("ADMIN");
            employee.setHourlyRate(BigDecimal.ZERO);
            employee.setSalesPercentage(BigDecimal.valueOf(5));
            employees.save(employee);
        }
    }
}
