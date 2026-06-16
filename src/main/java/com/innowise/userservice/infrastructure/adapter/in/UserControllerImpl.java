package com.innowise.userservice.infrastructure.adapter.in;

import com.innowise.userservice.application.dto.*;
import com.innowise.userservice.application.service.UserApplicationService;
import com.innowise.userservice.domain.model.exception.MaxPaymentCardsExceededException;
import com.innowise.userservice.domain.model.exception.UserNotFoundException;
import com.innowise.userservice.domain.port.in.UserController;
import com.innowise.userservice.infrastructure.security.model.JwtUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserControllerImpl implements UserController {

    private final UserApplicationService service;

    @PostMapping
    @Secured({"ADMIN", "SERVICE"})
    public ResponseEntity<Void> createUser(@RequestBody CreateUserDto createUserDto){
        UserResponseDto userResult = service.createUser(createUserDto);
        return ResponseEntity.created(URI.create("/api/v1/users/" + userResult.id())).build();
    }

    @PutMapping("/{userId}")
    @Secured({"ADMIN"})
    public ResponseEntity<MessageResponseDto> updateUser(@RequestBody UpdateUserDto updateUserDto,
                                                         @PathVariable("userId") Long userId) throws UserNotFoundException {
        service.updateUser(updateUserDto, userId);
        return ResponseEntity.ok(new MessageResponseDto("User updated successfully"));
    }

    @PostMapping("/{userId}/cards")
    @Secured({"USER","ADMIN", "SERVICE"})
    public ResponseEntity<MessageResponseDto> addCardByUserId(
            @RequestBody CreatePaymentCardDto createPaymentCardDto,
            @PathVariable("userId") Long userId,
            @AuthenticationPrincipal JwtUserDetails jwtUserDetails,
            Authentication authentication) throws UserNotFoundException, MaxPaymentCardsExceededException {
        boolean isAdminOrService = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("SERVICE"));
        PaymentCardResponseDto result = service.addCardByUserId(createPaymentCardDto, userId, jwtUserDetails.userId(), isAdminOrService);
        return ResponseEntity.created(URI.create("/api/v1/cards/" + result.id())).build();
    }

    @PatchMapping("/{userId}/deactivate")

    @Secured({"ADMIN"})
    public ResponseEntity<Void> deactivateUserById(@PathVariable("userId") Long id) throws UserNotFoundException {
        service.deactivateUserById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}/activate")
    @Secured({"ADMIN"})
    public ResponseEntity<Void> activateUserById(@PathVariable("userId") Long id) throws UserNotFoundException {
        service.activateUserById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Secured({"ADMIN"})
    public ResponseEntity<PageResponseDto<UserResponseDto>> getUsers(@ParameterObject UserFilter filter,
                                                                     @ParameterObject @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(service.getUsers(filter, pageable));
    }

    @GetMapping("/{userId}")
    @Secured({"ADMIN", "USER"})
    public ResponseEntity<FullUserResponseDto> getUserById(@PathVariable("userId") Long id,
            @AuthenticationPrincipal JwtUserDetails jwtUserDetails,
            Authentication authentication) throws UserNotFoundException {
        log.trace("Received a request to GET user by Id form user with userId: {} and roles: {}", id, authentication.getAuthorities());
        boolean isAdminOrUser = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("USER"));
        return ResponseEntity.ok(service.getUserById(id, jwtUserDetails.userId(), isAdminOrUser));
    }
}
