package dev.vorstu.controllers;

import dev.vorstu.dto.StudentDto;
import dev.vorstu.dto.StudentSelfUpdateDto;
import dev.vorstu.services.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/me/classmates")
    public ResponseEntity<List<StudentDto>> getClassmates(Principal principal) {
        return ResponseEntity.ok(studentService.getClassmates(principal.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<StudentDto> updateSelf(
            @Valid @RequestBody StudentSelfUpdateDto updateData,
            Principal principal) {
        return ResponseEntity.ok(studentService.updateSelf(principal.getName(), updateData));
    }
}
