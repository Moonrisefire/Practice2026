package dev.vorstu.controllers;

import dev.vorstu.dto.StudentDto;
import dev.vorstu.services.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public ResponseEntity<List<StudentDto>> getClassmates(@RequestParam(name = "group") String groupName) {
        return ResponseEntity.ok(studentService.getClassmates(groupName));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentDto> updateSelf(
            @PathVariable Long id,
            @Valid @RequestBody StudentDto updateData,
            Principal principal) {

        String currentUsername = principal.getName();

        return ResponseEntity.ok(studentService.updateSelf(id, currentUsername, updateData));
    }
}