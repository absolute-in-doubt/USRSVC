package com.innowise.userservice.application.mapper;

import com.innowise.userservice.application.dto.*;
import com.innowise.userservice.domain.model.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "active", constant = "true")
    User toEntity(CreateUserDto createUserDto);

    UserResponseDto toDto(User user);

    default FullUserResponseDto toFullDto(User user, List<PaymentCardResponseDto> cards) {
        return new FullUserResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.isActive(),
                cards
        );
    }

    void updateEntity(UpdateUserDto updateUserDto, @MappingTarget User user);
}
