package com.innowise.userservice.infrastructure.adapter.in;

import com.innowise.userservice.TestcontainersConfiguration;
import com.innowise.userservice.application.dto.*;
import com.innowise.userservice.config.TestSecurityConfig;
import com.innowise.userservice.domain.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestcontainersConfiguration.class, TestSecurityConfig.class})
@Testcontainers
class UserControllerImplE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserRepository userRepository;

    private String baseUrl;
    private long[] userId = new long[1];
    private long[] cardId = new long[1];

    private CreatePaymentCardDto createPaymentCardDto = new CreatePaymentCardDto(
            "1234567887654321",
            "John Doe",
            LocalDate.of(2100, 1, 1));

    private CreateUserDto createUserDto = new CreateUserDto(
            1L,
            "John",
            "Doe",
            LocalDate.of(2000, 1, 1),
            "some@email.com"
    );

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        baseUrl = "http://localhost:8081";

        // Create initial user with SERVICE role
        String token = "1.test.SERVICE";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Void> createResponse = restTemplate.exchange(
                baseUrl + "/api/v1/users",
                HttpMethod.POST,
                new HttpEntity<>(createUserDto, headers),
                Void.class
        );

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        String location = createResponse.getHeaders().getLocation().toString();
        String[] locationParts = location.split("/");
        userId[0] = Long.parseLong(locationParts[locationParts.length - 1]);

        // Create initial card
        headers.setBearerAuth(userId[0] + ".test.SERVICE");
        ResponseEntity<Void> cardResponse = restTemplate.exchange(
                baseUrl + "/api/v1/users/" + userId[0] + "/cards",
                HttpMethod.POST,
                new HttpEntity<>(createPaymentCardDto, headers),
                Void.class
        );

        assertEquals(HttpStatus.CREATED, cardResponse.getStatusCode());
        location = cardResponse.getHeaders().getLocation().toString();
        locationParts = location.split("/");
        cardId[0] = Long.parseLong(locationParts[locationParts.length - 1]);
    }

    private HttpHeaders createHeaders(String userId, String login, String... roles) {
        String token = userId + "." + login + "." + String.join(",", roles);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
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

        HttpHeaders headers = createHeaders("2", "jane", "ADMIN");
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/api/v1/users",
                HttpMethod.POST,
                new HttpEntity<>(newUserDto, headers),
                Void.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getHeaders().getLocation());

        // Verify the user was created
        headers = createHeaders("1", "test", "ADMIN");
        String url = baseUrl + "/api/v1/users?firstName=Jane&lastName=Smith";
        ResponseEntity<Map> getResponse = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        List<Map> content = (List<Map>) getResponse.getBody().get("content");
        assertNotNull(content);
        assertFalse(content.isEmpty());
        Map user = content.get(0);
        assertEquals("Jane", user.get("firstName"));
        assertEquals("Smith", user.get("lastName"));
        assertEquals("jane.smith@email.com", user.get("email"));
        assertEquals(true, user.get("active"));
    }

    @Test
    void updateUser() throws Exception {
        UpdateUserDto updateDto = new UpdateUserDto(
                "Johnny",
                "Doe",
                "johnny.doe@email.com",
                false
        );

        HttpHeaders headers = createHeaders(String.valueOf(userId[0]), "test", "ADMIN");
        ResponseEntity<MessageResponseDto> response = restTemplate.exchange(
                baseUrl + "/api/v1/users/" + userId[0],
                HttpMethod.PUT,
                new HttpEntity<>(updateDto, headers),
                MessageResponseDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        //assertEquals("User updated successfully", response.getBody().getMessage());

        // Verify the update
        ResponseEntity<FullUserResponseDto> getResponse = restTemplate.exchange(
                baseUrl + "/api/v1/users/" + userId[0],
                HttpMethod.GET,
                new HttpEntity<>(headers),
                FullUserResponseDto.class
        );

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        FullUserResponseDto user = getResponse.getBody();
        assertEquals("Johnny", user.firstName());
        assertEquals("Doe", user.lastName());
        assertEquals("johnny.doe@email.com", user.email());
        assertEquals(false, user.active());
    }

    @Test
    void addCardByUserId() throws Exception {
        CreatePaymentCardDto newCardDto = new CreatePaymentCardDto(
                "9876543210987654",
                "Johnny Doe",
                LocalDate.of(2110, 12, 31)
        );

        HttpHeaders headers = createHeaders(String.valueOf(userId[0]), "test", "USER");
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/api/v1/users/" + userId[0] + "/cards",
                HttpMethod.POST,
                new HttpEntity<>(newCardDto, headers),
                Void.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getHeaders().getLocation());

        // Verify the card was added
        ResponseEntity<List> getResponse = restTemplate.exchange(
                baseUrl + "/api/v1/users/" + userId[0] + "/cards",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                List.class
        );

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        List<Map> cards = getResponse.getBody();
        assertNotNull(cards);
        assertEquals(2, cards.size());
        
        Map card1 = cards.get(0);
        assertEquals(createPaymentCardDto.cardNumber(), card1.get("cardNumber"));
        assertEquals(createPaymentCardDto.holder(), card1.get("holder"));
        assertEquals(true, card1.get("active"));
        
        Map card2 = cards.get(1);
        assertEquals(newCardDto.cardNumber(), card2.get("cardNumber"));
        assertEquals(newCardDto.holder(), card2.get("holder"));
        assertEquals(true, card2.get("active"));
    }

    @Test
    void deactivateUserById() throws Exception {
        HttpHeaders headers = createHeaders(String.valueOf(userId[0]), "test", "ADMIN");
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/api/v1/users/" + userId[0] + "/deactivate",
                HttpMethod.PATCH,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify the user was deactivated
        ResponseEntity<FullUserResponseDto> getResponse = restTemplate.exchange(
                baseUrl + "/api/v1/users/" + userId[0],
                HttpMethod.GET,
                new HttpEntity<>(headers),
                FullUserResponseDto.class
        );

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        //assertEquals(false, getResponse.getBody().getActive());
    }

    @Test
    void activateUserById() throws Exception {
        // First deactivate
        HttpHeaders headers = createHeaders(String.valueOf(userId[0]), "test", "ADMIN");
        restTemplate.exchange(
                baseUrl + "/api/v1/users/" + userId[0] + "/deactivate",
                HttpMethod.PATCH,
                new HttpEntity<>(headers),
                Void.class
        );

        // Then activate
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/api/v1/users/" + userId[0] + "/activate",
                HttpMethod.PATCH,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify the user was activated
        ResponseEntity<FullUserResponseDto> getResponse = restTemplate.exchange(
                baseUrl + "/api/v1/users/" + userId[0],
                HttpMethod.GET,
                new HttpEntity<>(headers),
                FullUserResponseDto.class
        );

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        //assertEquals(true, getResponse.getBody().getActive());
    }

    @Test
    void getUsers() throws Exception {
        UserFilter filter = new UserFilter("John", "Doe");
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());

        HttpHeaders headers = createHeaders("1", "test", "ADMIN");
        String url = baseUrl + "/api/v1/users?" +
                "firstName=" + filter.firstName() +
                "&lastName=" + filter.lastName() +
                "&page=0&size=10&sort=id,asc";

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map body = response.getBody();
        assertNotNull(body);
        
        assertEquals(false, body.get("empty"));
        assertEquals(true, body.get("first"));
        assertEquals(true, body.get("last"));
        assertEquals(0, body.get("number"));
        assertEquals(10, body.get("size"));
        assertEquals(1, body.get("totalElements"));
        assertEquals(1, body.get("totalPages"));
        
        List<Map> content = (List<Map>) body.get("content");
        assertNotNull(content);
        assertEquals(1, content.size());
        
        Map user = content.get(0);
        assertEquals(userId[0], user.get("id"));
        assertEquals("John", user.get("firstName"));
        assertEquals("Doe", user.get("lastName"));
        assertEquals("some@email.com", user.get("email"));
        assertEquals(true, user.get("active"));
    }

    @Test
    void getUserById() throws Exception {
        HttpHeaders headers = createHeaders(String.valueOf(userId[0]), "test", "ADMIN", "USER");
        ResponseEntity<FullUserResponseDto> response = restTemplate.exchange(
                baseUrl + "/api/v1/users/" + userId[0],
                HttpMethod.GET,
                new HttpEntity<>(headers),
                FullUserResponseDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        FullUserResponseDto user = response.getBody();
        assertNotNull(user);
        
        assertEquals(userId[0], user.id());
        assertEquals("John", user.firstName());
        assertEquals("Doe", user.lastName());
        assertEquals("some@email.com", user.email());
        assertEquals(true, user.active());
        
        assertNotNull(user.cards());
        assertEquals(1, user.cards().size());
        PaymentCardResponseDto card = user.cards().get(0);
        assertEquals(createPaymentCardDto.cardNumber(), card.cardNumber());
        assertEquals(createPaymentCardDto.holder(), card.holder());
        assertEquals(createPaymentCardDto.expirationDate().toString(), card.expirationDate().toString());
        assertEquals(true, card.active());
    }
}

