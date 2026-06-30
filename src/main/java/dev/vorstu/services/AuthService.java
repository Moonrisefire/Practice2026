package dev.vorstu.services;

import dev.vorstu.dto.AuthRequest;
import dev.vorstu.dto.AuthResponse;
import dev.vorstu.models.BaseUser;
import dev.vorstu.repositories.UserRepository;
import dev.vorstu.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse authenticate(AuthRequest request) {
        BaseUser user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Неверный логин или пароль"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Неверный логин или пароль");
        }

        String jwtToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResponse refreshToken(String refreshToken) {
        String username = jwtUtil.extractUsername(refreshToken);

        if (username != null) {
            BaseUser user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            if (jwtUtil.isTokenValid(refreshToken, user.getUsername())) {
                String newAccessToken = jwtUtil.generateAccessToken(user);
                return AuthResponse.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(refreshToken)
                        .build();
            }
        }
        throw new RuntimeException("Невалидный Refresh Token");
    }
}