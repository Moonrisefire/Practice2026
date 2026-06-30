package dev.vorstu.mappers;

import dev.vorstu.dto.StudentDto;
import dev.vorstu.models.Student;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StudentMapper {
    StudentDto toDto(Student student);
    Student toEntity(StudentDto studentDto);
    void updateStudentFromDto(StudentDto dto, @MappingTarget Student student);
}