package dev.vorstu.services;

import dev.vorstu.dto.StudentDto;
import dev.vorstu.dto.StudentSelfUpdateDto;
import dev.vorstu.exceptions.ResourceNotFoundException;
import dev.vorstu.mappers.StudentMapper;
import dev.vorstu.models.Student;
import dev.vorstu.repositories.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    @Transactional(readOnly = true)
    public List<StudentDto> getClassmates(String currentUsername) {
        Student currentStudent = studentRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Студент не найден"));

        return studentRepository.findAllByGroupName(currentStudent.getGroupName()).stream()
                .map(studentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public StudentDto updateSelf(String currentUsername, StudentSelfUpdateDto updateData) {
        Student student = studentRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Студент не найден"));

        student.setFio(updateData.getFio());

        return studentMapper.toDto(studentRepository.save(student));
    }
}
