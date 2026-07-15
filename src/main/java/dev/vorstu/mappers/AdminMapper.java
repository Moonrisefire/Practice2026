package dev.vorstu.mappers;

import dev.vorstu.dto.AdminDto;
import dev.vorstu.models.Admin;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AdminMapper {
    @Mapping(target = "password", ignore = true)
    AdminDto toDto(Admin admin);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    Admin toEntity(AdminDto adminDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    void updateAdminFromDto(AdminDto dto, @MappingTarget Admin admin);
}
