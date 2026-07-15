package dev.vorstu.controllers;

import dev.vorstu.dto.AuthRequest;
import dev.vorstu.dto.AuthResponse;
import dev.vorstu.dto.ConfirmRegistrationResponse;
import dev.vorstu.dto.RefreshTokenRequest;
import dev.vorstu.services.AuthService;
import dev.vorstu.services.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RegistrationService registrationService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    @GetMapping("/confirm")
    public ResponseEntity<ConfirmRegistrationResponse> confirmRegistration(
            @RequestParam String token) {
        return ResponseEntity.ok(registrationService.confirmRegistration(token));
    }
}
