package com.kamui.luna.modules.auth.service;

import com.kamui.luna.core.exception.BusinessException;
import com.kamui.luna.core.exception.NotFoundException;
import com.kamui.luna.modules.auth.dto.GuestRegistrationRequest;
import com.kamui.luna.modules.auth.entity.Role;
import com.kamui.luna.modules.auth.entity.User;
import com.kamui.luna.modules.auth.repository.RoleRepository;
import com.kamui.luna.modules.auth.repository.UserRepository;
import com.kamui.luna.modules.loyalty.entity.Customer;
import com.kamui.luna.modules.loyalty.repository.CustomerRepository;
import com.kamui.luna.modules.staff.dto.EmployeeRegistrationRequest;
import com.kamui.luna.modules.staff.entity.Employee;
import com.kamui.luna.modules.staff.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;
import java.util.Locale;

@Service
public class AccountService {
    private final UserRepository users; private final RoleRepository roles; private final PasswordEncoder encoder;
    private final CustomerRepository customers; private final EmployeeRepository employees;
    public AccountService(UserRepository users, RoleRepository roles, PasswordEncoder encoder,
                          CustomerRepository customers, EmployeeRepository employees) {
        this.users=users; this.roles=roles; this.encoder=encoder; this.customers=customers; this.employees=employees;
    }

    @Transactional
    public Long registerGuest(GuestRegistrationRequest request) {
        User user = createUser(request.username(), request.password(), "GUEST");
        Customer customer = new Customer();
        customer.setUserId(user.getId()); customer.setFirstName(request.firstName().trim());
        customer.setPhoneNumber(request.phoneNumber());
        return customers.save(customer).getId();
    }

    @Transactional
    public Long registerEmployee(EmployeeRegistrationRequest request) {
        String role = request.role().toUpperCase(Locale.ROOT);
        if (!Set.of("ADMIN", "MANAGER", "WAITER").contains(role))
            throw new BusinessException("Для сотрудника допустимы роли ADMIN, MANAGER или WAITER");
        User user = createUser(request.username(), request.password(), role);
        Employee employee = new Employee(); employee.setUserId(user.getId());
        employee.setFirstName(request.firstName().trim()); employee.setLastName(request.lastName().trim());
        employee.setPosition(request.position().trim()); employee.setHourlyRate(request.hourlyRate());
        employee.setSalesPercentage(request.salesPercentage());
        return employees.save(employee).getId();
    }

    private User createUser(String username, String password, String roleName) {
        String normalized = username.trim().toLowerCase();
        if (users.existsByUsername(normalized)) throw new BusinessException("Логин уже занят");
        Role role = roles.findByName(roleName).orElseThrow(() -> new NotFoundException("Роль " + roleName + " не найдена"));
        User user = new User(); user.setUsername(normalized); user.setPasswordHash(encoder.encode(password));
        user.setActive(true); user.setRoles(Set.of(role));
        return users.save(user);
    }
}
