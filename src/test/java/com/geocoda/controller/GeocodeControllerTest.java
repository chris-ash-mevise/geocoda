package com.geocoda.controller;

import com.geocoda.model.GeocodingResult;
import com.geocoda.service.GeocodingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GeocodeControllerTest {

    private MockMvc mockMvc;
    private GeocodingService geocodingService;

    @BeforeEach
    void setUp() {
        geocodingService = mock(GeocodingService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new GeocodeController(geocodingService)).build();
    }

    @Test
    void geocodeReturnsResults() throws Exception {
        GeocodingResult result = new GeocodingResult(
                "Acme Corp", "123", "Main Street", "Springfield", "62701",
                39.7817, -89.6501, 1.5f
        );
        when(geocodingService.geocode("Main Street")).thenReturn(List.of(result));

        mockMvc.perform(get("/geocode").param("q", "Main Street"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].street").value("Main Street"))
                .andExpect(jsonPath("$[0].houseNumber").value("123"))
                .andExpect(jsonPath("$[0].city").value("Springfield"))
                .andExpect(jsonPath("$[0].latitude").value(39.7817))
                .andExpect(jsonPath("$[0].longitude").value(-89.6501));
    }

    @Test
    void geocodeReturnsEmptyListWhenNoMatch() throws Exception {
        when(geocodingService.geocode("nonexistent")).thenReturn(List.of());

        mockMvc.perform(get("/geocode").param("q", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void geocodeRequiresQueryParam() throws Exception {
        mockMvc.perform(get("/geocode"))
                .andExpect(status().isBadRequest());
    }
}
