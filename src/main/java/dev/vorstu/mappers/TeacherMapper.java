package dev.vorstu.mappers;

import dev.vorstu.dto.TeacherDto;
import dev.vorstu.models.Teacher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TeacherMapper {
    @Mapping(target = "password", ignore = true)
    TeacherDto toDto(Teacher teacher);
    Teacher toEntity(TeacherDto teacherDto);
    @Mapping(target = "password", ignore = true)
    void updateTeacherFromDto(TeacherDto dto, @MappingTarget Teacher teacher);
}