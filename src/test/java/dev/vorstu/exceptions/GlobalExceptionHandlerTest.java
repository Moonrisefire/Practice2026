package dev.vorstu.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleNotFound_returns404() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleNotFound(new ResourceNotFoundException("Не найден"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Не найден", response.getBody().get("error"));
    }

    @Test
    void handleUnauthorized_returns401() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleUnauthorized(new UnauthorizedException("Не авторизован"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void handleConflict_returns409() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleConflict(new ConflictException("Конфликт"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleGone_returns410() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleGone(new GoneException("Истёк срок"));

        assertEquals(HttpStatus.GONE, response.getStatusCode());
    }
}
