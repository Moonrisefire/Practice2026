package dev.vorstu.mappers;

import dev.vorstu.dto.StudentDto;
import dev.vorstu.models.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StudentMapper {
    @Mapping(target = "password", ignore = true)
    StudentDto toDto(Student student);
    Student toEntity(StudentDto studentDto);
    @Mapping(target = "password", ignore = true)
    void updateStudentFromDto(StudentDto dto, @MappingTarget Student student);
}