package dev.vorstu;

import dev.vorstu.models.Role;
import dev.vorstu.models.Student;
import dev.vorstu.repositories.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        if (studentRepository.count() == 0) {

            Student student1 = new Student();
            student1.setUsername("user1");
            student1.setPassword(passwordEncoder.encode("1234"));
            student1.setRole(Role.STUDENT);
            student1.setFio("Иванов Иван");
            student1.setGroupName("VM");

            Student student2 = new Student();
            student2.setUsername("user2");
            student2.setPassword(passwordEncoder.encode("1234"));
            student2.setRole(Role.STUDENT);
            student2.setFio("Петров Петр");
            student2.setGroupName("VM");

            Student student3 = new Student();
            student3.setUsername("user3");
            student3.setPassword(passwordEncoder.encode("1234"));
            student3.setRole(Role.STUDENT);
            student3.setFio("Сидоров Алексей");
            student3.setGroupName("AM");

            studentRepository.save(student1);
            studentRepository.save(student2);
            studentRepository.save(student3);

            System.out.println("Стартовые студенты успешно добавлены в PostgreSQL и пароли захешированы!");
        }
    }
}