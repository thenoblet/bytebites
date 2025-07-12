package gtp.restaurant;

import gtp.bytebites.restaurant.model.Restaurant;
import gtp.bytebites.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class RestaurantControllerIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @BeforeEach
    void setUp() {
        restaurantRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_OWNER")
    void createRestaurant_withValidRequest_shouldReturn201() throws Exception {
        String requestJson = """
        {
            "name": "Test Restaurant",
            "address": "123 Test St",
            "description": "Test description",
            "ownerId": "550e8400-e29b-41d4-a716-446655440000",
            "cuisineType": "Italian",
            "menuItems": [
                {
                    "name": "Spaghetti Bolognese",
                    "description": "Classic Italian pasta dish",
                    "price": 15.99
                },
                {
                    "name": "Pizza Margherita",
                    "description": "Tomato, mozzarella, and basil",
                    "price": 12.50
                }
            ]
        }
        """;

        mockMvc.perform(post("/api/v1/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.success").value(true),
                        jsonPath("$.data.name").value("Test Restaurant"),
                        jsonPath("$.data.cuisineType").value("Italian"),
                        jsonPath("$.message").value("Restaurant created successfully"),
                        header().exists("Location")
                );

        assertEquals(1, restaurantRepository.count());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void createRestaurant_withCustomerRole_shouldReturn403() throws Exception {
        String requestJson = """
        {
            "name": "Test Restaurant For Customer",
            "address": "456 Test Ave",
            "description": "Another test description",
            "ownerId": "550e8400-e29b-41d4-a716-446655440000",
            "cuisineType": "Mexican",
            "menuItems": [
                {
                    "name": "Taco",
                    "description": "Delicious taco",
                    "price": 3.50
                }
            ]
        }
        """;

        mockMvc.perform(post("/api/v1/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());

        assertEquals(0, restaurantRepository.count());
    }

    @Test
    @WithMockUser
    void getAllRestaurants_withPagination_shouldReturnCorrectPage() throws Exception {
        UUID ownerId = UUID.randomUUID();
        Restaurant restaurant1 = new Restaurant();
        restaurant1.setName("A Restaurant");
        restaurant1.setAddress("123 St");
        restaurant1.setOwnerId(ownerId);
        restaurant1.setCuisineType("Italian");

        Restaurant restaurant2 = new Restaurant();
        restaurant2.setName("B Restaurant");
        restaurant2.setAddress("456 St");
        restaurant2.setOwnerId(ownerId);
        restaurant2.setCuisineType("French");

        restaurantRepository.saveAll(List.of(restaurant1, restaurant2));

        mockMvc.perform(get("/api/v1/restaurants?page=0&size=1&sort=name,asc"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.success").value(true),
                        jsonPath("$.data.content[0].name").value("A Restaurant"),
                        jsonPath("$.data.totalElements").value(2),
                        jsonPath("$.data.pageable.pageSize").value(1),
                        jsonPath("$.data.content[0].address").value("123 St")
                );
    }

    @Test
    void getAllRestaurants_unauthenticated_shouldReturn401() throws Exception {
        UUID ownerId = UUID.randomUUID();
        Restaurant restaurant = new Restaurant();
        restaurant.setName("A Restaurant");
        restaurant.setAddress("123 St");
        restaurant.setOwnerId(ownerId);
        restaurant.setCuisineType("Italian");

        restaurantRepository.save(restaurant);

        mockMvc.perform(get("/api/v1/restaurants"))
                .andExpect(status().isOk());
    }
}