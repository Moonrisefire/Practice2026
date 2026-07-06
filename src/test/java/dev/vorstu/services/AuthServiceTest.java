package dev.vorstu.services;

import dev.vorstu.dto.AuthRequest;
import dev.vorstu.dto.AuthResponse;
import dev.vorstu.exceptions.UnauthorizedException;
import dev.vorstu.models.Admin;
import dev.vorstu.models.Role;
import dev.vorstu.repositories.UserRepository;
import dev.vorstu.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private Admin admin;

    @BeforeEach
    void setUp() {
        admin = new Admin();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPassword("encoded");
        admin.setRole(Role.ADMIN);
    }

    @Test
    void authenticate_success() {
        AuthRequest request = new AuthRequest();
        request.setUsername("admin");
        request.setPassword("1234");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("1234", "encoded")).thenReturn(true);
        when(jwtUtil.generateAccessToken(admin)).thenReturn("access");
        when(jwtUtil.generateRefreshToken(admin)).thenReturn("refresh");

        AuthResponse response = authService.authenticate(request);

        assertEquals("access", response.getAccessToken());
        assertEquals("refresh", response.getRefreshToken());
    }

    @Test
    void authenticate_invalidPassword() {
        AuthRequest request = new AuthRequest();
        request.setUsername("admin");
        request.setPassword("wrong");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.authenticate(request));
    }

    @Test
    void refreshToken_success() {
        when(jwtUtil.isRefreshToken("refresh")).thenReturn(true);
        when(jwtUtil.extractUsername("refresh")).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(jwtUtil.isTokenValid("refresh", "admin")).thenReturn(true);
        when(jwtUtil.generateAccessToken(admin)).thenReturn("new-access");

        AuthResponse response = authService.refreshToken("refresh");

        assertNotNull(response.getAccessToken());
        assertEquals("refresh", response.getRefreshToken());
    }

    @Test
    void refreshToken_rejectsAccessToken() {
        when(jwtUtil.isRefreshToken("access")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.refreshToken("access"));
    }
}
