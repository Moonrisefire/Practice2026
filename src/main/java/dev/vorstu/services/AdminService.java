package dev.vorstu.services;

import dev.vorstu.dto.AdminDto;
import dev.vorstu.dto.AssignGroupRequest;
import dev.vorstu.dto.StudentDto;
import dev.vorstu.dto.TeacherDto;
import dev.vorstu.exceptions.BadRequestException;
import dev.vorstu.exceptions.ConflictException;
import dev.vorstu.exceptions.ResourceNotFoundException;
import dev.vorstu.mappers.AdminMapper;
import dev.vorstu.mappers.StudentMapper;
import dev.vorstu.mappers.TeacherMapper;
import dev.vorstu.models.Admin;
import dev.vorstu.models.Role;
import dev.vorstu.models.Student;
import dev.vorstu.models.Teacher;
import dev.vorstu.repositories.AdminRepository;
import dev.vorstu.repositories.StudentRepository;
import dev.vorstu.repositories.TeacherRepository;
import dev.vorstu.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;

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

    @Transactional(readOnly = true)
    public List<AdminDto> getAllAdmins() {
        return adminRepository.findAll().stream()
                .map(adminMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminDto getAdminById(Long id) {
        return adminRepository.findById(id)
                .map(adminMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Администратор не найден"));
    }

    @Transactional
    public TeacherDto assignGroupToTeacher(Long teacherId, AssignGroupRequest request) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Преподаватель не найден"));

        teacher.getAssignedGroups().add(request.getGroupName());
        return teacherMapper.toDto(teacherRepository.save(teacher));
    }

    @Transactional
    public void deleteStudent(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Студент не найден");
        }
        studentRepository.deleteById(studentId);
    }

    @Transactional
    public StudentDto createStudent(StudentDto studentDto) {
        if (studentDto.getPassword() == null || studentDto.getPassword().isBlank()) {
            throw new BadRequestException("Пароль обязателен при создании");
        }
        if (userRepository.findByUsername(studentDto.getUsername()).isPresent()) {
            throw new ConflictException("Пользователь с таким именем уже существует");
        }

        Student student = studentMapper.toEntity(studentDto);
        student.setRole(Role.STUDENT);
        student.setPassword(passwordEncoder.encode(studentDto.getPassword()));
        return studentMapper.toDto(studentRepository.save(student));
    }

    @Transactional
    public StudentDto updateStudent(Long id, StudentDto updateData) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Студент не найден"));

        studentMapper.updateStudentFromDto(updateData, student);

        if (updateData.getPassword() != null && !updateData.getPassword().isBlank()) {
            student.setPassword(passwordEncoder.encode(updateData.getPassword()));
        }

        return studentMapper.toDto(studentRepository.save(student));
    }

    @Transactional
    public TeacherDto createTeacher(TeacherDto teacherDto) {
        if (teacherDto.getPassword() == null || teacherDto.getPassword().isBlank()) {
            throw new BadRequestException("Пароль обязателен при создании");
        }
        String username = teacherDto.getUsername() != null ? teacherDto.getUsername() : teacherDto.getEmail();
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ConflictException("Пользователь с таким именем уже существует");
        }
        if (teacherRepository.existsByEmail(teacherDto.getEmail())) {
            throw new ConflictException("Преподаватель с таким email уже существует");
        }

        Teacher teacher = teacherMapper.toEntity(teacherDto);
        teacher.setUsername(username);
        teacher.setRole(Role.TEACHER);
        teacher.setPassword(passwordEncoder.encode(teacherDto.getPassword()));
        return teacherMapper.toDto(teacherRepository.save(teacher));
    }

    @Transactional
    public TeacherDto updateTeacher(Long id, TeacherDto updateData) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Преподаватель не найден"));

        teacherMapper.updateTeacherFromDto(updateData, teacher);

        if (updateData.getPassword() != null && !updateData.getPassword().isBlank()) {
            teacher.setPassword(passwordEncoder.encode(updateData.getPassword()));
        }

        return teacherMapper.toDto(teacherRepository.save(teacher));
    }

    @Transactional
    public void deleteTeacher(Long teacherId) {
        if (!teacherRepository.existsById(teacherId)) {
            throw new ResourceNotFoundException("Преподаватель не найден");
        }
        teacherRepository.deleteById(teacherId);
    }

    @Transactional
    public AdminDto createAdmin(AdminDto adminDto) {
        if (userRepository.findByUsername(adminDto.getUsername()).isPresent()) {
            throw new ConflictException("Пользователь с таким именем уже существует");
        }

        Admin admin = adminMapper.toEntity(adminDto);
        admin.setRole(Role.ADMIN);
        admin.setPassword(passwordEncoder.encode(adminDto.getPassword()));
        return adminMapper.toDto(adminRepository.save(admin));
    }

    @Transactional
    public AdminDto updateAdmin(Long id, AdminDto updateData) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Администратор не найден"));

        if (updateData.getUsername() != null) {
            admin.setUsername(updateData.getUsername());
        }
        if (updateData.getPassword() != null && !updateData.getPassword().isBlank()) {
            admin.setPassword(passwordEncoder.encode(updateData.getPassword()));
        }

        return adminMapper.toDto(adminRepository.save(admin));
    }

    @Transactional
    public void deleteAdmin(Long id) {
        if (!adminRepository.existsById(id)) {
            throw new ResourceNotFoundException("Администратор не найден");
        }
        adminRepository.deleteById(id);
    }
}
