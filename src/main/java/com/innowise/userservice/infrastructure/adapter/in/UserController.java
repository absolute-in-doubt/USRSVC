package com.innowise.userservice.infrastructure.adapter.in;

import com.innowise.userservice.application.dto.*;
import com.innowise.userservice.application.service.UserApplicationService;
import com.innowise.userservice.domain.model.exception.MaxPaymentCardsExceededException;
import com.innowise.userservice.domain.model.exception.UserNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Validated
public class UserController {

    private final UserApplicationService service;

    @PostMapping
    public ResponseEntity<MessageResponseDto> createUser(@RequestBody @Valid CreateUserDto createUserDto){
        service.createUser(createUserDto);
        return ResponseEntity.ok(new MessageResponseDto("User created successfully"));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<MessageResponseDto> updateUser(@RequestBody @Valid UpdateUserDto updateUserDto,
                                                         @PathVariable("userId") Long userId) throws UserNotFoundException {
        service.updateUser(updateUserDto, userId);
        return ResponseEntity.ok(new MessageResponseDto("User updated successfully"));
    }

    @PostMapping("/{userId}/cards")
    public ResponseEntity<MessageResponseDto> addCardByUserId(@Valid CreatePaymentCardDto createPaymentCardDto,
                                                              @PathVariable("userId") Long userId) throws UserNotFoundException, MaxPaymentCardsExceededException {
        service.addCardByUserId(createPaymentCardDto, userId);
        return ResponseEntity.ok(new MessageResponseDto("Card added successfully"));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<MessageResponseDto> deactivateUserById(@PathVariable("id") Long id) throws UserNotFoundException {
        service.deactivateUserById(id);
        return ResponseEntity.ok(new MessageResponseDto("User deactivated successfully"));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<MessageResponseDto> activateUserById(@PathVariable("id") Long id) throws UserNotFoundException {
        service.activateUserById(id);
        return ResponseEntity.ok(new MessageResponseDto("User activated successfully"));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponseDto>> getUsers(@ParameterObject @Valid UserFilter filter,
                                                          @ParameterObject @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(service.getUsers(filter, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable("id") Long id) throws UserNotFoundException {
        return ResponseEntity.ok(service.getUserById(id));
    }
}
