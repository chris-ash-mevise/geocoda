package com.geocoda.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GeocodingResultTest {

    @Test
    void formattedAddressWithAllFields() {
        GeocodingResult result = new GeocodingResult(
                "Test", "123", "Main St", "Springfield", "62701",
                39.78, -89.65, 1.0f);

        assertEquals("123 Main St, Springfield 62701", result.getFormattedAddress());
    }

    @Test
    void formattedAddressWithoutHouseNumber() {
        GeocodingResult result = new GeocodingResult();
        result.setStreet("Oak Lane");
        result.setCity("Portland");

        assertEquals("Oak Lane, Portland", result.getFormattedAddress());
    }

    @Test
    void formattedAddressEmpty() {
        GeocodingResult result = new GeocodingResult();
        assertEquals("", result.getFormattedAddress());
    }

    @Test
    void gettersAndSetters() {
        GeocodingResult r = new GeocodingResult();
        r.setName("Place");
        r.setHouseNumber("42");
        r.setStreet("Elm St");
        r.setCity("Denver");
        r.setPostcode("80201");
        r.setLatitude(39.74);
        r.setLongitude(-104.99);
        r.setScore(2.5f);

        assertEquals("Place", r.getName());
        assertEquals("42", r.getHouseNumber());
        assertEquals("Elm St", r.getStreet());
        assertEquals("Denver", r.getCity());
        assertEquals("80201", r.getPostcode());
        assertEquals(39.74, r.getLatitude(), 0.001);
        assertEquals(-104.99, r.getLongitude(), 0.001);
        assertEquals(2.5f, r.getScore(), 0.001);
    }
}
