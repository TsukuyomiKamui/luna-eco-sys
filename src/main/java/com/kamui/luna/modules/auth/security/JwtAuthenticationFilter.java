package com.kamui.luna.modules.auth.security;

import com.kamui.luna.modules.auth.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger filterLogger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    // Внедряем зависимости через конструктор
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, CustomUserDetailsService customUserDetailsService) {
        this.tokenProvider = tokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. Достаем токен из HTTP-запроса
            String jwt = getJwtFromRequest(request);

            // 2. Если токен есть и он прошел криптографическую проверку
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {

                // 3. Достаем имя пользователя из токена
                String username = tokenProvider.getUsernameFromJWT(jwt);

                // 4. Загружаем пользователя и его права из базы данных
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                // 5. Собираем объект аутентификации для Spring Security
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. Кладем пользователя в контекст безопасности (теперь он "залогинен" на время этого запроса)
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            filterLogger.error("Не удалось установить аутентификацию пользователя в контексте безопасности", ex);
        }

        // 7. Передаем запрос дальше по цепочке фильтров
        filterChain.doFilter(request, response);
    }

    /**
     * Вспомогательный метод для извлечения сырого токена из заголовка Authorization.
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // Проверяем, что заголовок не пустой и начинается с классического префикса "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}