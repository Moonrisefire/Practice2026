package dev.vorstu.services;

import dev.vorstu.dto.StudentDto;
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
    public List<StudentDto> getClassmates(String groupName) {
        return studentRepository.findAllByGroupName(groupName).stream()
                .map(studentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public StudentDto updateSelf(Long studentId, StudentDto updateData) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Студент не найден"));

        if (updateData.getFio() != null) {
            student.setFio(updateData.getFio());
        }

        return studentMapper.toDto(studentRepository.save(student));
    }
}