package com.innowise.userservice.application.dto;

public record UpdateUserDto(Long id,
                           String name,
                           String surname,
                           String email,
                            boolean active) {
}
