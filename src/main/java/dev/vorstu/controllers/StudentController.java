package dev.vorstu.controllers;

import dev.vorstu.dto.StudentDto;
import dev.vorstu.services.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping(value = "/classmates/{groupName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<StudentDto> getClassmates(@PathVariable String groupName) {
        return studentService.getClassmates(groupName);
    }

    @PutMapping(value = "/me", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public StudentDto updateSelf(@RequestBody StudentDto updateData) {
        Long currentUserId = 1L;

        return studentService.updateSelf(currentUserId, updateData);
    }
}