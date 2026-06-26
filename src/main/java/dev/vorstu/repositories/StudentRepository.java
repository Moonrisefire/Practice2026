package dev.vorstu.repositories;

import dev.vorstu.dto.Student;
import org.springframework.data.repository.CrudRepository;

public interface StudentRepository extends CrudRepository<Student, Long> {
    Iterable<Student> findByGroup(String group);
}