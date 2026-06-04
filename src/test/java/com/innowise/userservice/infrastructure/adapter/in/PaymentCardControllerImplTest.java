package com.innowise.userservice.infrastructure.adapter.in;


import com.innowise.userservice.TestcontainersConfiguration;
import com.innowise.userservice.application.dto.CreatePaymentCardDto;
import com.innowise.userservice.application.dto.CreateUserDto;
import com.innowise.userservice.application.dto.PaymentCardFilter;
import com.innowise.userservice.application.dto.UpdatePaymentCardDto;
import com.innowise.userservice.domain.port.out.PaymentCardRepository;
import com.innowise.userservice.domain.port.out.UserRepository;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@Slf4j
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Testcontainers
@AutoConfigureMockMvc
class PaymentCardControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

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
                .with(jwt().jwt(jwtBuilder -> jwtBuilder.subject("1").claim("login", "test").claim("roles", List.of("SERVICE"))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createUserDto))).andDo( r -> {
                    String[] locationParts = r.getResponse().getHeader("location").split("/");
                    userId[0] = Long.parseLong(locationParts[locationParts.length-1]);
        });

        mockMvc.perform(post("/api/v1/users/" + userId[0] + "/cards")
                .with(jwt().jwt(jwtBuilder -> jwtBuilder.subject("" + userId[0]).claim("login", "test").claim("roles", List.of("SERVICE"))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createPaymentCardDto)
        )).andDo( r -> {
                    String[] locationParts = r.getResponse().getHeader("location").split("/");
                    cardId[0] = Long.parseLong(locationParts[locationParts.length-1]);
        });
        log.trace("Set up card with cardId: {}", cardId[0]);
        log.trace("All the cards that are currently in the DB: {}", paymentCardRepository.findAll());
    }

    @Test
    void getCardById() throws Exception {
        mockMvc.perform(get("/api/v1/cards/" + cardId[0])
                .with(jwt().jwt(jwtBuilder -> jwtBuilder.subject("" + userId[0]).claim("login", "test").claim("roles", List.of("USER")))))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.cardNumber").value(createPaymentCardDto.cardNumber()))
                .andExpect(jsonPath("$.holder").value(createPaymentCardDto.holder()))
                .andExpect(jsonPath("$.expirationDate").value(createPaymentCardDto.expirationDate().toString()))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getAllCards() throws Exception {
        PaymentCardFilter filter = new PaymentCardFilter("John", "Doe");
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());

        mockMvc.perform(get("/api/v1/cards")
                .with(jwt().jwt(jwtBuilder -> jwtBuilder.subject("" + userId[0]).claim("login", "test").claim("roles", List.of("ADMIN"))))
                .param("userFirstName", filter.userFirstName())
                .param("userLastName", filter.userLastName())
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
                .andExpect(jsonPath("$.content[0].cardNumber").value(createPaymentCardDto.cardNumber()))
                .andExpect(jsonPath("$.content[0].holder").value(createPaymentCardDto.holder()))
                .andExpect(jsonPath("$.content[0].expirationDate").value(createPaymentCardDto.expirationDate().toString()))
                .andExpect(jsonPath("$.content[0].active").value(true));
    }

    @Test
    void getCardsByUserId() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + userId[0] + "/cards")
                .with(jwt().jwt(jwtBuilder -> jwtBuilder.subject("" + userId[0]).claim("login", "test").claim("roles", List.of("USER")))))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$[0].cardNumber").value(createPaymentCardDto.cardNumber()))
                .andExpect(jsonPath("$[0].holder").value(createPaymentCardDto.holder()))
                .andExpect(jsonPath("$[0].expirationDate").value(createPaymentCardDto.expirationDate().toString()))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void deactivateCardById() throws Exception {
        mockMvc.perform(patch("/api/v1/cards/" + cardId[0] + "/deactivate")
                .with(jwt().jwt(jwtBuilder -> jwtBuilder.subject("" + userId[0]).claim("login", "test").claim("roles", List.of("USER")))))
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

        mockMvc.perform(get("/api/v1/cards/" + cardId[0])
                .with(jwt().jwt(jwtBuilder -> jwtBuilder.subject("" + userId[0]).claim("login", "test").claim("roles", List.of("USER")))))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void activateCardById() throws Exception {
        mockMvc.perform(patch("/api/v1/cards/" + cardId[0] + "/deactivate")
                .with(jwt().jwt(jwtBuilder -> jwtBuilder.subject("" + userId[0]).claim("login", "test").claim("roles", List.of("USER")))))
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

        mockMvc.perform(patch("/api/v1/cards/" + cardId[0] + "/activate")
                .with(jwt().jwt(jwtBuilder -> jwtBuilder.subject("" + userId[0]).claim("login", "test").claim("roles", List.of("USER")))))
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

        mockMvc.perform(get("/api/v1/cards/" + cardId[0])
                .with(jwt().jwt(jwtBuilder -> jwtBuilder.subject("" + userId[0]).claim("login", "test").claim("roles", List.of("USER")))))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void updateCard() throws Exception {

        log.trace("All the cards that are currently in the DB: {}", paymentCardRepository.findAll());

        UpdatePaymentCardDto updateDto = new UpdatePaymentCardDto(
                "1111222233334444",
                "Jane Doe",
                LocalDate.of(2125, 12, 31),
                false
        );

        mockMvc.perform(put("/api/v1/cards/" + cardId[0])
                .with(jwt().jwt(jwtBuilder -> jwtBuilder.subject("" + userId[0]).claim("login", "test").claim("roles", List.of("USER"))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.message").value("Card updated successfully"));

        mockMvc.perform(get("/api/v1/cards/" + cardId[0])
                .with(jwt().jwt(jwtBuilder -> jwtBuilder.subject("" + userId[0]).claim("login", "test").claim("roles", List.of("USER")))))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.cardNumber").value(updateDto.cardNumber()))
                .andExpect(jsonPath("$.holder").value(updateDto.holder()))
                .andExpect(jsonPath("$.expirationDate").value(updateDto.expirationDate().toString()))
                .andExpect(jsonPath("$.active").value(updateDto.active()));
    }
}
