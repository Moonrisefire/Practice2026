package dev.vorstu.controllers;

import dev.vorstu.dto.AdminDto;
import dev.vorstu.dto.AssignGroupRequest;
import dev.vorstu.dto.CsvUploadResultDto;
import dev.vorstu.dto.StudentCreateDto;
import dev.vorstu.dto.StudentDto;
import dev.vorstu.dto.TeacherCreateDto;
import dev.vorstu.dto.TeacherDto;
import dev.vorstu.services.AdminService;
import dev.vorstu.services.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final RegistrationService registrationService;

    @GetMapping("/students")
    public ResponseEntity<List<StudentDto>> getAllStudents() {
        return ResponseEntity.ok(adminService.getAllStudents());
    }

    @GetMapping("/teachers")
    public ResponseEntity<List<TeacherDto>> getAllTeachers() {
        return ResponseEntity.ok(adminService.getAllTeachers());
    }

    @GetMapping("/admins")
    public ResponseEntity<List<AdminDto>> getAllAdmins() {
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    @GetMapping("/admins/{id}")
    public ResponseEntity<AdminDto> getAdminById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getAdminById(id));
    }

    @PostMapping("/students")
    public ResponseEntity<StudentDto> createStudent(@Valid @RequestBody StudentCreateDto studentDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createStudent(studentDto.toStudentDto()));
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<StudentDto> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentDto studentDto) {
        return ResponseEntity.ok(adminService.updateStudent(id, studentDto));
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        adminService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/teachers")
    public ResponseEntity<TeacherDto> createTeacher(@Valid @RequestBody TeacherCreateDto teacherDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createTeacher(teacherDto.toTeacherDto()));
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

    @PostMapping("/teachers/{id}/groups")
    public ResponseEntity<TeacherDto> assignGroup(
            @PathVariable Long id,
            @Valid @RequestBody AssignGroupRequest request) {
        return ResponseEntity.ok(adminService.assignGroupToTeacher(id, request));
    }

    @PostMapping("/admins")
    public ResponseEntity<AdminDto> createAdmin(@Valid @RequestBody AdminDto adminDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createAdmin(adminDto));
    }

    @PutMapping("/admins/{id}")
    public ResponseEntity<AdminDto> updateAdmin(
            @PathVariable Long id,
            @Valid @RequestBody AdminDto adminDto) {
        return ResponseEntity.ok(adminService.updateAdmin(id, adminDto));
    }

    @DeleteMapping("/admins/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/registration-requests", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CsvUploadResultDto> uploadRegistrationCsv(
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registrationService.processCsvUpload(file));
    }
}
