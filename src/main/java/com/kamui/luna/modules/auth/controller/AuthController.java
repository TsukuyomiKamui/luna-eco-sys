package com.kamui.luna.modules.auth.controller;

import com.kamui.luna.modules.auth.dto.LoginRequest;
import com.kamui.luna.modules.auth.dto.JwtResponse;
import com.kamui.luna.modules.auth.dto.TokenRefreshRequest;
import com.kamui.luna.modules.auth.entity.RefreshToken;
import com.kamui.luna.modules.auth.entity.User;
import com.kamui.luna.modules.auth.security.JwtTokenProvider;
import com.kamui.luna.modules.auth.service.RefreshTokenService;
import com.kamui.luna.modules.auth.repository.RefreshTokenRepository;
import com.kamui.luna.modules.auth.repository.UserRepository;
import com.kamui.luna.modules.auth.service.CurrentUserService;
import com.kamui.luna.modules.auth.service.AccountService;
import com.kamui.luna.modules.auth.dto.GuestRegistrationRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUser;
    private final AccountService accountService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider,
                          RefreshTokenService refreshTokenService,
                          RefreshTokenRepository refreshTokenRepository,
                          UserRepository userRepository, CurrentUserService currentUser, AccountService accountService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
        this.accountService = accountService;
    }

    /**
     * ПЕРВЫЙ ЭНДПОИНТ: Первичный вход (Логин)
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        // 1. Проверяем подлинность пары Логин/Пароль через Spring Security
        String username = loginRequest.username().trim().toLowerCase();
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        loginRequest.password()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 2. Генерируем короткий Access Token (на 15 минут) для заголовков запросов
        String accessToken = tokenProvider.generateToken(authentication);

        // 3. Достаем нашего доменного пользователя из базы данных
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new com.kamui.luna.core.exception.NotFoundException("Пользователь не найден после аутентификации"));

        // 4. Ищем существующий в базе Refresh-токен.
        // Если это самый первый вход сотрудника — генерируем ему новый токен на 7 дней.
        String refreshTokenStr = refreshTokenService.getOrCreate(user).getToken();

        // 5. Собираем права доступа (Permissions)
        Set<String> permissions = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return ResponseEntity.ok(new JwtResponse(accessToken, refreshTokenStr, permissions));
    }

    /**
     * ВТОРОЙ ЭНДПОИНТ: Обновление протухшей сессии (Тихий рефреш)
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.refreshToken();

        return refreshTokenRepository.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration) // Проверяем, жив ли токен
                .map(RefreshToken::getUser) // Достаем владельца токена
                .map(user -> {
                    // Распаковываем права пользователя из ролей для сборки нового Access токена
                    List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                            .flatMap(role -> role.getPermissions().stream())
                            .map(p -> new SimpleGrantedAuthority(p.getName()))
                            .collect(Collectors.toList());

                    var auth = new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);
                    String newAccessToken = tokenProvider.generateToken(auth);

                    Set<String> perms = authorities.stream()
                            .map(SimpleGrantedAuthority::getAuthority)
                            .collect(Collectors.toSet());

                    // МАТЧИНГ: Возвращаем новый короткий токен и сохраняем старый рефреш на устройстве
                    return ResponseEntity.ok(new JwtResponse(newAccessToken, requestRefreshToken, perms));
                })
                .orElseThrow(() -> new com.kamui.luna.core.exception.NotFoundException("Предоставленный Refresh token отсутствует в базе данных"));
    }

    @PostMapping("/register-guest")
    public ResponseEntity<Long> registerGuest(@Valid @RequestBody GuestRegistrationRequest request) {
        return ResponseEntity.status(201).body(accountService.registerGuest(request));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout() {
        refreshTokenService.deleteByUserId(currentUser.id());
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }
}
