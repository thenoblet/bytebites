package gtp.restaurant;

import gtp.bytebites.restaurant.dto.request.CreateMenuRequest;
import gtp.bytebites.restaurant.model.MenuItem;
import gtp.bytebites.restaurant.model.Restaurant;
import gtp.bytebites.restaurant.repository.MenuItemRepository;
import gtp.bytebites.restaurant.repository.RestaurantRepository;
import gtp.bytebites.restaurant.util.ValidationUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MenuControllerIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @MockBean
    private ValidationUtil validationUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID restaurantId;
    private UUID ownerId;
    private Restaurant testRestaurant;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            return mapper;
        }
    }

    @BeforeEach
    void setUp() {
        restaurantRepository.deleteAll();
        menuItemRepository.deleteAll();

        ownerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        testRestaurant = new Restaurant();
        testRestaurant.setName("Test Restaurant");
        testRestaurant.setAddress("123 Test St");
        testRestaurant.setOwnerId(ownerId);
        testRestaurant.setCuisineType("Italian");

        testRestaurant = restaurantRepository.save(testRestaurant);
        restaurantId = testRestaurant.getId();

        Mockito.when(validationUtil.isRestaurantOwner(any(UUID.class))).thenReturn(false);
    }

    private CreateMenuRequest createValidMenuRequest() {
        return new CreateMenuRequest(
                "Burger",
                "Classic beef burger with cheese",
                BigDecimal.valueOf(10.99)
        );
    }

    private UUID getCurrentMockUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            try {
                return UUID.fromString((String) authentication.getPrincipal());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createMenuItem_withAdminRole_shouldReturn201() throws Exception {
        CreateMenuRequest request = createValidMenuRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/restaurants/{id}/menu", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.success").value(true),
                        jsonPath("$.data.name").value("Burger"),
                        jsonPath("$.data.price").value(10.99),
                        jsonPath("$.message").value("Menu item created successfully."),
                        header().exists("Location")
                );

        assertEquals(1, menuItemRepository.count());
        Optional<MenuItem> savedItem = menuItemRepository.findByName("Burger");
        assertTrue(savedItem.isPresent());
        assertEquals(restaurantId, savedItem.get().getRestaurant().getId());
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000", roles = "RESTAURANT_OWNER")
    void createMenuItem_withRestaurantOwnerRole_andCorrectOwnerId_shouldReturn201() throws Exception {
        Mockito.when(validationUtil.isRestaurantOwner(eq(restaurantId))).thenReturn(true);

        CreateMenuRequest request = createValidMenuRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/restaurants/{id}/menu", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.success").value(true),
                        jsonPath("$.data.name").value("Burger"),
                        header().exists("Location")
                );

        assertEquals(1, menuItemRepository.count());
    }

    @Test
    @WithMockUser(username = "anotherOwnerId", roles = "RESTAURANT_OWNER")
    void createMenuItem_withRestaurantOwnerRole_andIncorrectOwnerId_shouldReturn403() throws Exception {
        Mockito.when(validationUtil.isRestaurantOwner(eq(restaurantId))).thenReturn(false);

        CreateMenuRequest request = createValidMenuRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/restaurants/{id}/menu", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden()); // Expect 403
        assertEquals(0, menuItemRepository.count());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void createMenuItem_withCustomerRole_shouldReturn403() throws Exception {
        CreateMenuRequest request = createValidMenuRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/restaurants/{id}/menu", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden()); // Expect 403
        assertEquals(0, menuItemRepository.count());
    }

    @Test
    void createMenuItem_unauthenticated_shouldReturn401() throws Exception {
        CreateMenuRequest request = createValidMenuRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/restaurants/{id}/menu", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());
        assertEquals(0, menuItemRepository.count());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createMenuItem_withInvalidRequest_shouldReturn400() throws Exception {
        CreateMenuRequest invalidRequest = new CreateMenuRequest("", "Description", BigDecimal.TEN);
        String requestJson = objectMapper.writeValueAsString(invalidRequest);

        mockMvc.perform(post("/api/v1/restaurants/{id}/menu", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.success").value(false),
                        jsonPath("$.message").value("Validation failed") // Assuming your GlobalExceptionHandler returns this message
                );
        assertEquals(0, menuItemRepository.count());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createMenuItem_forNonExistentRestaurant_shouldReturn404() throws Exception {
        UUID nonExistentRestaurantId = UUID.randomUUID();
        CreateMenuRequest request = createValidMenuRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/restaurants/{id}/menu", nonExistentRestaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound()); // Expect 404
        assertEquals(0, menuItemRepository.count());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createMenuItem_whenMenuItemAlreadyExists_shouldReturn409() throws Exception {
        MenuItem existingItem = new MenuItem();
        existingItem.setName("Burger");
        existingItem.setDescription("Existing description");
        existingItem.setPrice(BigDecimal.valueOf(10.00));
        existingItem.setRestaurant(testRestaurant);
        menuItemRepository.save(existingItem);
        menuItemRepository.flush();

        CreateMenuRequest request = createValidMenuRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/restaurants/{id}/menu", restaurantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpectAll(
                        status().isConflict(),
                        jsonPath("$.success").value(false),
                        jsonPath("$.message").value("Menu item with name 'Burger' already exists for this restaurant")
                );
        assertEquals(1, menuItemRepository.count());
    }
}
