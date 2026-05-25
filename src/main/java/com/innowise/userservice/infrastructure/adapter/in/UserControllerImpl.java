package com.innowise.userservice.infrastructure.adapter.in;

import com.innowise.userservice.application.dto.*;
import com.innowise.userservice.application.service.UserApplicationService;
import com.innowise.userservice.domain.model.exception.MaxPaymentCardsExceededException;
import com.innowise.userservice.domain.model.exception.UserNotFoundException;
import com.innowise.userservice.domain.port.in.UserController;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserControllerImpl implements UserController {

    private final UserApplicationService service;

    @PostMapping
    public ResponseEntity<Void> createUser(@RequestBody CreateUserDto createUserDto){
        UserResponseDto userResult = service.createUser(createUserDto);
        return ResponseEntity.created(URI.create("/api/v1/users/" + userResult.id())).build();
    }

    @PutMapping("/{userId}")
    public ResponseEntity<MessageResponseDto> updateUser(@RequestBody UpdateUserDto updateUserDto,
                                                         @PathVariable("userId") Long userId) throws UserNotFoundException {
        service.updateUser(updateUserDto, userId);
        return ResponseEntity.ok(new MessageResponseDto("User updated successfully"));
    }

    @PostMapping("/{userId}/cards")
    public ResponseEntity<MessageResponseDto> addCardByUserId(@RequestBody CreatePaymentCardDto createPaymentCardDto,
                                                              @PathVariable("userId") Long userId) throws UserNotFoundException, MaxPaymentCardsExceededException {
        service.addCardByUserId(createPaymentCardDto, userId);
        return ResponseEntity.ok(new MessageResponseDto("Card added successfully"));
    }

    @PatchMapping("/{userId}/deactivate")
    public ResponseEntity<Void> deactivateUserById(@PathVariable("userId") Long id) throws UserNotFoundException {
        service.deactivateUserById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}/activate")
    public ResponseEntity<Void> activateUserById(@PathVariable("userId") Long id) throws UserNotFoundException {
        service.activateUserById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<UserResponseDto>> getUsers(@ParameterObject UserFilter filter,
                                                          @ParameterObject @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(service.getUsers(filter, pageable));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<FullUserResponseDto> getUserById(@PathVariable("userId") Long id) throws UserNotFoundException {
        return ResponseEntity.ok(service.getUserById(id));
    }
}
