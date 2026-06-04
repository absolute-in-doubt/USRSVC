package com.innowise.userservice.infrastructure.adapter.in;

import com.innowise.userservice.TestcontainersConfiguration;
import com.innowise.userservice.application.dto.*;
import com.innowise.userservice.config.TestSecurityConfig;
import com.innowise.userservice.domain.port.out.PaymentCardRepository;
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
class PaymentCardControllerImplE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

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
    void getCardById() throws Exception {
        HttpHeaders headers = createHeaders(String.valueOf(userId[0]), "test", "USER");
        ResponseEntity<PaymentCardResponseDto> response = restTemplate.exchange(
                baseUrl + "/api/v1/cards/" + cardId[0],
                HttpMethod.GET,
                new HttpEntity<>(headers),
                PaymentCardResponseDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        PaymentCardResponseDto card = response.getBody();
        assertNotNull(card);
        
        assertEquals(createPaymentCardDto.cardNumber(), card.cardNumber());
        assertEquals(createPaymentCardDto.holder(), card.holder());
        assertEquals(createPaymentCardDto.expirationDate().toString(), card.expirationDate().toString());
        assertEquals(true, card.active());
    }

    @Test
    void getAllCards() throws Exception {
        PaymentCardFilter filter = new PaymentCardFilter("John", "Doe");
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());

        HttpHeaders headers = createHeaders(String.valueOf(userId[0]), "test", "ADMIN");
        String url = baseUrl + "/api/v1/cards?" +
                "userFirstName=" + filter.userFirstName() +
                "&userLastName=" + filter.userLastName() +
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
        
        Map card = content.get(0);
        assertEquals(createPaymentCardDto.cardNumber(), card.get("cardNumber"));
        assertEquals(createPaymentCardDto.holder(), card.get("holder"));
        assertEquals(createPaymentCardDto.expirationDate().toString(), card.get("expirationDate"));
        assertEquals(true, card.get("active"));
    }

    @Test
    void getCardsByUserId() throws Exception {
        HttpHeaders headers = createHeaders(String.valueOf(userId[0]), "test", "USER");
        ResponseEntity<List> response = restTemplate.exchange(
                baseUrl + "/api/v1/users/" + userId[0] + "/cards",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                List.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Map> cards = response.getBody();
        assertNotNull(cards);
        assertEquals(1, cards.size());
        
        Map card = cards.get(0);
        assertEquals(createPaymentCardDto.cardNumber(), card.get("cardNumber"));
        assertEquals(createPaymentCardDto.holder(), card.get("holder"));
        assertEquals(createPaymentCardDto.expirationDate().toString(), card.get("expirationDate"));
        assertEquals(true, card.get("active"));
    }

    @Test
    void deactivateCardById() throws Exception {
        HttpHeaders headers = createHeaders(String.valueOf(userId[0]), "test", "USER");
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/api/v1/cards/" + cardId[0] + "/deactivate",
                HttpMethod.PATCH,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify the card was deactivated
        ResponseEntity<PaymentCardResponseDto> getResponse = restTemplate.exchange(
                baseUrl + "/api/v1/cards/" + cardId[0],
                HttpMethod.GET,
                new HttpEntity<>(headers),
                PaymentCardResponseDto.class
        );

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals(false, getResponse.getBody().active());
    }

    @Test
    void activateCardById() throws Exception {
        // First deactivate
        HttpHeaders headers = createHeaders(String.valueOf(userId[0]), "test", "USER");
        restTemplate.exchange(
                baseUrl + "/api/v1/cards/" + cardId[0] + "/deactivate",
                HttpMethod.PATCH,
                new HttpEntity<>(headers),
                Void.class
        );

        // Then activate
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/api/v1/cards/" + cardId[0] + "/activate",
                HttpMethod.PATCH,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify the card was activated
        ResponseEntity<PaymentCardResponseDto> getResponse = restTemplate.exchange(
                baseUrl + "/api/v1/cards/" + cardId[0],
                HttpMethod.GET,
                new HttpEntity<>(headers),
                PaymentCardResponseDto.class
        );

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals(true, getResponse.getBody().active());
    }

    @Test
    void updateCard() throws Exception {
        UpdatePaymentCardDto updateDto = new UpdatePaymentCardDto(
                "1111222233334444",
                "Jane Doe",
                LocalDate.of(2125, 12, 31),
                false
        );

        HttpHeaders headers = createHeaders(String.valueOf(userId[0]), "test", "USER");
        ResponseEntity<MessageResponseDto> response = restTemplate.exchange(
                baseUrl + "/api/v1/cards/" + cardId[0],
                HttpMethod.PUT,
                new HttpEntity<>(updateDto, headers),
                MessageResponseDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        //assertEquals("Card updated successfully", response.getBody().getMessage());

        // Verify the update
        ResponseEntity<PaymentCardResponseDto> getResponse = restTemplate.exchange(
                baseUrl + "/api/v1/cards/" + cardId[0],
                HttpMethod.GET,
                new HttpEntity<>(headers),
                PaymentCardResponseDto.class
        );

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        PaymentCardResponseDto card = getResponse.getBody();
        assertNotNull(card);
        
        assertEquals(updateDto.cardNumber(), card.cardNumber());
        assertEquals(updateDto.holder(), card.holder());
        assertEquals(updateDto.expirationDate().toString(), card.expirationDate().toString());
        assertEquals(updateDto.active(), card.active());
    }
}

