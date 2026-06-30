package dev.vorstu.mappers;

import dev.vorstu.dto.AdminDto;
import dev.vorstu.models.Admin;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AdminMapper {
    AdminDto toDto(Admin admin);
    Admin toEntity(AdminDto adminDto);
}