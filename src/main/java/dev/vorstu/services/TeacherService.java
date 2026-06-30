package dev.vorstu.services;

import dev.vorstu.dto.StudentDto;
import dev.vorstu.mappers.StudentMapper;
import dev.vorstu.models.Student;
import dev.vorstu.models.Teacher;
import dev.vorstu.repositories.StudentRepository;
import dev.vorstu.repositories.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    @Transactional(readOnly = true)
    public List<StudentDto> getStudentsByTeacher(Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Преподаватель не найден"));

        return studentRepository.findAllByGroupNameIn(teacher.getAssignedGroups())
                .stream()
                .map(studentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public StudentDto updateStudentByTeacher(Long teacherId, Long studentId, StudentDto updateData) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Преподаватель не найден"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Студент не найден"));

        if (!teacher.getAssignedGroups().contains(student.getGroupName())) {
            throw new RuntimeException("Нет доступа: студент находится в чужой группе");
        }

        if (updateData.getFio() != null) student.setFio(updateData.getFio());
        if (updateData.getGroupName() != null) student.setGroupName(updateData.getGroupName());

        return studentMapper.toDto(studentRepository.save(student));
    }
}