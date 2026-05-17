package com.innowise.userservice.application.mapper;

import com.innowise.userservice.application.dto.CreateUserDto;
import com.innowise.userservice.application.dto.UpdateUserDto;
import com.innowise.userservice.application.dto.UserResponseDto;
import com.innowise.userservice.domain.model.User;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    User toEntity(CreateUserDto createUserDto);

    UserResponseDto toDto(User user);

    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    void updateEntity(UpdateUserDto updateUserDto, @MappingTarget User user);
}
