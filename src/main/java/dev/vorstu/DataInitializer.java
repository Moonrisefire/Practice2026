package dev.vorstu;

import dev.vorstu.models.Admin;
import dev.vorstu.models.Role;
import dev.vorstu.models.Student;
import dev.vorstu.models.Teacher;
import dev.vorstu.repositories.UserRepository;
import dev.vorstu.repositories.StudentRepository;
import dev.vorstu.repositories.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        if (userRepository.findByUsername("admin").isPresent()) {
            log.info("База данных уже содержит пользователей. Инициализация пропущена.");
            return;
        }

        log.info("Начинаем загрузку тестовых данных...");

        Admin admin = new Admin();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("1234"));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        Teacher teacher = new Teacher();
        teacher.setUsername("teacher_ivanov");
        teacher.setPassword(passwordEncoder.encode("secure_pass_1"));
        teacher.setRole(Role.TEACHER);
        teacher.setFio("Иванов Иван Иванович");
        teacher.setEmail("ivanov@univer.ru");
        teacher.setPhone("+79001234567");
        teacher.setAssignedGroups(Set.of("VM"));
        teacherRepository.save(teacher);

        Student student1 = new Student();
        student1.setUsername("student_petrov");
        student1.setPassword(passwordEncoder.encode("secure_pass_2"));
        student1.setRole(Role.STUDENT);
        student1.setFio("Петров Петр");
        student1.setGroupName("VM");
        studentRepository.save(student1);

        Student student2 = new Student();
        student2.setUsername("student_ivanov");
        student2.setPassword(passwordEncoder.encode("1234"));
        student2.setRole(Role.STUDENT);
        student2.setFio("Иванов Илья");
        student2.setGroupName("VM");
        studentRepository.save(student2);

        Student student3 = new Student();
        student3.setUsername("student_sidorov");
        student3.setPassword(passwordEncoder.encode("1234"));
        student3.setRole(Role.STUDENT);
        student3.setFio("Сидоров Алексей");
        student3.setGroupName("AM");
        studentRepository.save(student3);

        log.info("Тестовые данные успешно загружены! Пароли зашифрованы.");
    }
}