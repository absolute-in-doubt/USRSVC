package com.innowise.userservice.infrastructure.adapter.in;

import com.innowise.userservice.TestcontainersConfiguration;
import com.innowise.userservice.application.dto.CreatePaymentCardDto;
import com.innowise.userservice.application.dto.CreateUserDto;
import com.innowise.userservice.application.dto.UserFilter;
import com.innowise.userservice.application.dto.PageResponseDto;
import com.innowise.userservice.application.dto.UserResponseDto;
import com.innowise.userservice.application.dto.UpdateUserDto;
import com.innowise.userservice.application.dto.FullUserResponseDto;
import com.innowise.userservice.application.dto.MessageResponseDto;
import com.innowise.userservice.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Testcontainers
@AutoConfigureMockMvc
class UserControllerImplTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserRepository userRepository;

    private CreatePaymentCardDto createPaymentCardDto = new CreatePaymentCardDto(
            "1234567887654321",
            "John Doe",
            LocalDate.of(2100, 1, 1));

    private CreateUserDto createUserDto = new CreateUserDto(
            1L,
            "John",
            "Doe",
            LocalDate.of(2000, 1,1),
            "some@email.com"
    );
    private long[] userId = new long[1];
    private long[] cardId = new long[1];

    @BeforeEach
    public void setUp() throws Exception{
        userRepository.deleteAll();

        mockMvc.perform(post("/api/v1/users")
                .with(jwt().authorities(new SimpleGrantedAuthority("SERVICE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createUserDto))).andDo( r -> {
            String[] locationParts = r.getResponse().getHeader("location").split("/");
            userId[0] = Long.parseLong(locationParts[locationParts.length-1]);
        });

        mockMvc.perform(post("/api/v1/users/" + userId[0] + "/cards")
                .with(jwt().authorities(new SimpleGrantedAuthority("SERVICE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createPaymentCardDto)
                )).andDo( r -> {
            String[] locationParts = r.getResponse().getHeader("location").split("/");
            cardId[0] = Long.parseLong(locationParts[locationParts.length-1]);
        });
    }


    @Test
    void createUser() throws Exception {
        CreateUserDto newUserDto = new CreateUserDto(
                2L,
                "Jane",
                "Smith",
                LocalDate.of(1995, 5, 15),
                "jane.smith@email.com"
        );

        mockMvc.perform(post("/api/v1/users")
                .with(jwt().authorities(new SimpleGrantedAuthority("ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(newUserDto)))
                .andExpect(status().is(HttpStatus.CREATED.value()))
                .andExpect(header().exists("location"));

        mockMvc.perform(get("/api/v1/users")
                .with(jwt().authorities(new SimpleGrantedAuthority("ADMIN")))
                .param("firstName", "Jane")
                .param("lastName", "Smith"))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.content[0].firstName").value("Jane"))
                .andExpect(jsonPath("$.content[0].lastName").value("Smith"))
                .andExpect(jsonPath("$.content[0].email").value("jane.smith@email.com"))
                .andExpect(jsonPath("$.content[0].active").value(true));
    }

    @Test
    void updateUser() throws Exception {
        UpdateUserDto updateDto = new UpdateUserDto(
                "Johnny",
                "Doe",
                "johnny.doe@email.com",
                false
        );

        mockMvc.perform(put("/api/v1/users/" + userId[0])
                .with(jwt().authorities(new SimpleGrantedAuthority("ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("User updated successfully"));

        mockMvc.perform(get("/api/v1/users/" + userId[0])
                .with(jwt().authorities(new SimpleGrantedAuthority("ADMIN"))))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.firstName").value("Johnny"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("johnny.doe@email.com"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void addCardByUserId() throws Exception {
        CreatePaymentCardDto newCardDto = new CreatePaymentCardDto(
                "9876543210987654",
                "Johnny Doe",
                LocalDate.of(2110, 12, 31)
        );

        mockMvc.perform(post("/api/v1/users/" + userId[0] + "/cards")
                .with(jwt().authorities(new SimpleGrantedAuthority("USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(newCardDto)))
                .andExpect(status().is(HttpStatus.CREATED.value()))
                .andExpect(header().exists("location"));

        mockMvc.perform(get("/api/v1/users/" + userId[0] + "/cards")
                .with(jwt().authorities(new SimpleGrantedAuthority("USER"))))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$[0].cardNumber").value(createPaymentCardDto.cardNumber()))
                .andExpect(jsonPath("$[0].holder").value(createPaymentCardDto.holder()))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[1].cardNumber").value(newCardDto.cardNumber()))
                .andExpect(jsonPath("$[1].holder").value(newCardDto.holder()))
                .andExpect(jsonPath("$[1].active").value(true));
    }

    @Test
    void deactivateUserById() throws Exception {
        mockMvc.perform(patch("/api/v1/users/" + userId[0] + "/deactivate")
                .with(jwt().authorities(new SimpleGrantedAuthority("ADMIN"))))
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

        mockMvc.perform(get("/api/v1/users/" + userId[0])
                .with(jwt().authorities(new SimpleGrantedAuthority("ADMIN"))))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void activateUserById() throws Exception {
        mockMvc.perform(patch("/api/v1/users/" + userId[0] + "/deactivate")
                .with(jwt().authorities(new SimpleGrantedAuthority("ADMIN"))))
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

        mockMvc.perform(patch("/api/v1/users/" + userId[0] + "/activate")
                .with(jwt().authorities(new SimpleGrantedAuthority("ADMIN"))))
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

        mockMvc.perform(get("/api/v1/users/" + userId[0])
                .with(jwt().authorities(new SimpleGrantedAuthority("ADMIN"))))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getUsers() throws Exception {
        UserFilter filter = new UserFilter("John", "Doe");
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());

        mockMvc.perform(get("/api/v1/users")
                .with(jwt().authorities(new SimpleGrantedAuthority("ADMIN")))
                .param("firstName", filter.firstName())
                .param("lastName", filter.lastName())
                .param("page", "0")
                .param("size", "10")
                .param("sort", "id,asc"))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.empty").value(false))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].id").value(userId[0]))
                .andExpect(jsonPath("$.content[0].firstName").value("John"))
                .andExpect(jsonPath("$.content[0].lastName").value("Doe"))
                .andExpect(jsonPath("$.content[0].email").value("some@email.com"))
                .andExpect(jsonPath("$.content[0].active").value(true));
    }

    @Test
    void getUserById() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + userId[0])
                .with(jwt().authorities(new SimpleGrantedAuthority("ADMIN"))))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.id").value(userId[0]))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("some@email.com"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.cards[0].cardNumber").value(createPaymentCardDto.cardNumber()))
                .andExpect(jsonPath("$.cards[0].holder").value(createPaymentCardDto.holder()))
                .andExpect(jsonPath("$.cards[0].expirationDate").value(createPaymentCardDto.expirationDate().toString()))
                .andExpect(jsonPath("$.cards[0].active").value(true));
    }
}
