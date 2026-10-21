package com.kamui.luna.modules.auth.service;

import com.kamui.luna.core.exception.NotFoundException;
import com.kamui.luna.modules.auth.entity.User;
import com.kamui.luna.modules.auth.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) { this.userRepository = userRepository; }

    public User get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new NotFoundException("Пользователь не авторизован");
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    public Long id() { return get().getId(); }
}
