package dev.vorstu.controllers;

import dev.vorstu.dto.StudentDto;
import dev.vorstu.dto.TeacherDto;
import dev.vorstu.services.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/students")
    public ResponseEntity<List<StudentDto>> getAllStudents() {
        return ResponseEntity.ok(adminService.getAllStudents());
    }

    @GetMapping("/teachers")
    public ResponseEntity<List<TeacherDto>> getAllTeachers() {
        return ResponseEntity.ok(adminService.getAllTeachers());
    }

    @PostMapping("/teachers/{id}/groups")
    public ResponseEntity<TeacherDto> assignGroup(
            @PathVariable Long id,
            @RequestParam String groupName) {
        return ResponseEntity.ok(adminService.assignGroupToTeacher(id, groupName));
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        adminService.deleteStudent(id);
        return ResponseEntity.noContent().build(); // Возвращаем 204 No Content при успешном удалении
    }

    @PostMapping("/students")
    public ResponseEntity<StudentDto> createStudent(@Valid @RequestBody StudentDto studentDto) {
        // HTTP 201 Created — правильный статус для успешного создания ресурса
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createStudent(studentDto));
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<StudentDto> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentDto studentDto) {
        return ResponseEntity.ok(adminService.updateStudent(id, studentDto));
    }

    @PostMapping("/teachers")
    public ResponseEntity<TeacherDto> createTeacher(@Valid @RequestBody TeacherDto teacherDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createTeacher(teacherDto));
    }

    @PutMapping("/teachers/{id}")
    public ResponseEntity<TeacherDto> updateTeacher(
            @PathVariable Long id,
            @Valid @RequestBody TeacherDto teacherDto) {
        return ResponseEntity.ok(adminService.updateTeacher(id, teacherDto));
    }

    @DeleteMapping("/teachers/{id}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        adminService.deleteTeacher(id);
        return ResponseEntity.noContent().build();
    }
}