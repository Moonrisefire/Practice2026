package dev.vorstu.services;

import dev.vorstu.dto.AuthRequest;
import dev.vorstu.dto.AuthResponse;
import dev.vorstu.exceptions.UnauthorizedException;
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
                .orElseThrow(() -> new UnauthorizedException("Неверный логин или пароль"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Неверный логин или пароль");
        }

        return AuthResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(user))
                .refreshToken(jwtUtil.generateRefreshToken(user))
                .build();
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Невалидный Refresh Token");
        }

        String username = jwtUtil.extractUsername(refreshToken);
        if (username == null) {
            throw new UnauthorizedException("Невалидный Refresh Token");
        }

        BaseUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Пользователь не найден"));

        if (!jwtUtil.isTokenValid(refreshToken, user.getUsername())) {
            throw new UnauthorizedException("Невалидный Refresh Token");
        }

        return AuthResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(user))
                .refreshToken(refreshToken)
                .build();
    }
}
