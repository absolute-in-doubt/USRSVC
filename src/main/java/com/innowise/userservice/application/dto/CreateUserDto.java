package com.innowise.userservice.application.dto;

import java.time.LocalDate;

public record CreateUserDto(String name,
                            String surname,
                            LocalDate birthDate,
                            String email) {
    //active is automatically set to true when the user is created (in mapper)
}
