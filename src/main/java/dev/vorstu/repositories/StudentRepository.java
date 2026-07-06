package dev.vorstu.repositories;

import dev.vorstu.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findAllByGroupName(String groupName);
    List<Student> findAllByGroupNameIn(java.util.Set<String> groupNames);
    Optional<Student> findByUsername(String username);
}
