package dev.vorstu.services;

import dev.vorstu.dto.StudentDto;
import dev.vorstu.dto.TeacherDto;
import dev.vorstu.mappers.StudentMapper;
import dev.vorstu.mappers.TeacherMapper;
import dev.vorstu.models.Role;
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
public class AdminService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;

    @Transactional(readOnly = true)
    public List<StudentDto> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(studentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeacherDto> getAllTeachers() {
        return teacherRepository.findAll().stream()
                .map(teacherMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TeacherDto assignGroupToTeacher(Long teacherId, String groupName) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Преподаватель не найден"));

        teacher.getAssignedGroups().add(groupName);

        return teacherMapper.toDto(teacherRepository.save(teacher));
    }

    @Transactional
    public void deleteStudent(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new RuntimeException("Студент не найден");
        }
        studentRepository.deleteById(studentId);
    }

    @Transactional
    public StudentDto createStudent(StudentDto studentDto) {
        Student student = studentMapper.toEntity(studentDto);
        student.setRole(Role.STUDENT);
        student.setPassword("temp_password_123"); /// TODO Временно хардкодим пароль
        return studentMapper.toDto(studentRepository.save(student));
    }

    @Transactional
    public StudentDto updateStudent(Long id, StudentDto updateData) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Студент не найден"));

        studentMapper.updateStudentFromDto(updateData, student);

        return studentMapper.toDto(studentRepository.save(student));
    }

    @Transactional
    public TeacherDto createTeacher(TeacherDto teacherDto) {
        Teacher teacher = teacherMapper.toEntity(teacherDto);
        teacher.setRole(Role.TEACHER);
        teacher.setPassword("temp_password_123"); /// TODO Временно хардкодим пароль
        return teacherMapper.toDto(teacherRepository.save(teacher));
    }

    @Transactional
    public TeacherDto updateTeacher(Long id, TeacherDto updateData) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Преподаватель не найден"));

        teacherMapper.updateTeacherFromDto(updateData, teacher);

        return teacherMapper.toDto(teacherRepository.save(teacher));
    }

    @Transactional
    public void deleteTeacher(Long teacherId) {
        if (!teacherRepository.existsById(teacherId)) {
            throw new RuntimeException("Преподаватель не найден");
        }
        teacherRepository.deleteById(teacherId);
    }
}