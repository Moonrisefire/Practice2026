package dev.vorstu;

import dev.vorstu.dto.Student;
import dev.vorstu.repositories.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Initializer {
    @Autowired
    private StudentRepository studentRepository;

    public Initializer(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public void initial() {
        if (studentRepository.count() == 0) {
            studentRepository.save(new Student("User1", "VM", "+7"));
            studentRepository.save(new Student("User2", "VM", "+8"));
            studentRepository.save(new Student("User3", "AM", "+99"));
            System.out.println("Стартовые студенты успешно добавлены в PostgreSQL!");
        }
    }
}
