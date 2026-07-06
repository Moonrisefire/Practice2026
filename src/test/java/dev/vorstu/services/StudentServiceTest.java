package dev.vorstu.services;

import dev.vorstu.dto.StudentDto;
import dev.vorstu.exceptions.ResourceNotFoundException;
import dev.vorstu.mappers.StudentMapper;
import dev.vorstu.models.Student;
import dev.vorstu.repositories.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private StudentMapper studentMapper;

    @InjectMocks
    private StudentService studentService;

    @Test
    void getClassmates_returnsOnlyOwnGroup() {
        Student current = new Student();
        current.setUsername("student1");
        current.setGroupName("VM");

        Student classmate = new Student();
        classmate.setId(2L);
        classmate.setFio("Иванов");

        StudentDto dto = new StudentDto();
        dto.setId(2L);
        dto.setFio("Иванов");

        when(studentRepository.findByUsername("student1")).thenReturn(Optional.of(current));
        when(studentRepository.findAllByGroupName("VM")).thenReturn(List.of(current, classmate));
        when(studentMapper.toDto(current)).thenReturn(new StudentDto());
        when(studentMapper.toDto(classmate)).thenReturn(dto);

        List<StudentDto> result = studentService.getClassmates("student1");

        assertEquals(2, result.size());
        assertEquals("Иванов", result.get(1).getFio());
    }

    @Test
    void getClassmates_studentNotFound() {
        when(studentRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> studentService.getClassmates("unknown"));
    }
}
