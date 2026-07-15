package dev.vorstu.controllers;

import dev.vorstu.dto.StudentDto;
import dev.vorstu.dto.StudentTeacherUpdateDto;
import dev.vorstu.services.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping("/me/students")
    public ResponseEntity<List<StudentDto>> getMyStudents(Principal principal) {
        return ResponseEntity.ok(teacherService.getStudentsByTeacher(principal.getName()));
    }

    @PutMapping("/me/students/{studentId}")
    public ResponseEntity<StudentDto> updateStudent(
            @PathVariable Long studentId,
            @Valid @RequestBody StudentTeacherUpdateDto updateData,
            Principal principal) {
        return ResponseEntity.ok(teacherService.updateStudentByTeacher(principal.getName(), studentId, updateData));
    }
}
