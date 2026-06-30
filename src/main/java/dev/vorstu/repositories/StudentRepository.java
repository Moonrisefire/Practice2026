package dev.vorstu.repositories;

import dev.vorstu.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findAllByGroupName(String group);
    List<Student> findAllByGroupNameIn(Collection<String> groupNames);
}