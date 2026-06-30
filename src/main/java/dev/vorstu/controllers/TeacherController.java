package dev.vorstu.controllers;

import dev.vorstu.dto.StudentDto;
import dev.vorstu.services.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping("/{teacherId}/students")
    public ResponseEntity<List<StudentDto>> getMyStudents(@PathVariable Long teacherId) {
        return ResponseEntity.ok(teacherService.getStudentsByTeacher(teacherId));
    }

    @PutMapping("/{teacherId}/students/{studentId}")
    public ResponseEntity<StudentDto> updateStudent(
            @PathVariable Long teacherId,
            @PathVariable Long studentId,
            @Valid @RequestBody StudentDto updateData) {
        return ResponseEntity.ok(teacherService.updateStudentByTeacher(teacherId, studentId, updateData));
    }
}