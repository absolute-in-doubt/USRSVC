package com.innowise.userservice.application.mapper;

import com.innowise.userservice.application.dto.CreateUserDto;
import com.innowise.userservice.application.dto.UpdateUserDto;
import com.innowise.userservice.application.dto.UserResponseDto;
import com.innowise.userservice.domain.model.User;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "active", constant = "true")
    User toEntity(CreateUserDto createUserDto);

    UserResponseDto toDto(User user);

    void updateEntity(UpdateUserDto updateUserDto, @MappingTarget User user);
}
