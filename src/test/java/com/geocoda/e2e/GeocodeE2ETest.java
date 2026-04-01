package com.geocoda.e2e;

import com.geocoda.model.GeocodingResult;
import com.geocoda.service.LuceneIndexService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end tests that start the full Spring Boot application on a random port
 * and exercise the HTTP API with real HTTP calls through the entire stack:
 * HTTP request → Controller → Service → Lucene Index → HTTP response.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "geocoda.index-dir=${java.io.tmpdir}/geocoda-e2e-test-index-${random.uuid}",
        "geocoda.data-dir=${java.io.tmpdir}/geocoda-e2e-test-data-${random.uuid}",
        "geocoda.pbf-file-path="
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GeocodeE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private LuceneIndexService indexService;

    @BeforeAll
    void seedTestData() throws Exception {
        indexService.addDocument("Acme Corp", "123", "Main Street", "Springfield", "62701",
                39.7817, -89.6501);
        indexService.addDocument(null, "456", "Broadway Avenue", "New York", "10001",
                40.7484, -73.9967);
        indexService.addDocument("City Hall", "1", "Market Street", "San Francisco", "94103",
                37.7749, -122.4194);
        indexService.commit();
    }

    @Test
    void helloEndpointReturns200WithGreeting() {
        ResponseEntity<String> response = restTemplate.getForEntity("/hello", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Hello, World!", response.getBody());
    }

    @Test
    void geocodeReturnsResultsForStreetSearch() {
        ResponseEntity<List<GeocodingResult>> response = restTemplate.exchange(
                "/geocode?q={q}", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {}, "Main Street");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<GeocodingResult> results = response.getBody();
        assertNotNull(results);
        assertFalse(results.isEmpty(), "Should find results for 'Main Street'");
        assertTrue(results.stream().anyMatch(r -> "Main Street".equals(r.getStreet())),
                "Results should include Main Street");
    }

    @Test
    void geocodeReturnsEmptyListWhenNoMatch() {
        ResponseEntity<List<GeocodingResult>> response = restTemplate.exchange(
                "/geocode?q={q}", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {}, "xyznonexistent12345");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<GeocodingResult> results = response.getBody();
        assertNotNull(results);
        assertTrue(results.isEmpty(), "Should return empty list for nonsense query");
    }

    @Test
    void geocodeFuzzyMatchWorksEndToEnd() {
        // "Brodway" is a typo for "Broadway"
        ResponseEntity<List<GeocodingResult>> response = restTemplate.exchange(
                "/geocode?q={q}", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {}, "Brodway");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<GeocodingResult> results = response.getBody();
        assertNotNull(results);
        assertFalse(results.isEmpty(), "Fuzzy matching should find 'Broadway' from 'Brodway'");
        assertTrue(results.stream().anyMatch(r -> "Broadway Avenue".equals(r.getStreet())),
                "Fuzzy results should include Broadway Avenue");
    }

    @Test
    void geocodeReturnsCorrectJsonStructure() {
        ResponseEntity<List<GeocodingResult>> response = restTemplate.exchange(
                "/geocode?q={q}", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {}, "Main Street Springfield");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<GeocodingResult> results = response.getBody();
        assertNotNull(results);
        assertFalse(results.isEmpty());

        GeocodingResult result = results.stream()
                .filter(r -> "Main Street".equals(r.getStreet()))
                .findFirst()
                .orElse(null);
        assertNotNull(result, "Expected to find Main Street in results");

        assertEquals("Acme Corp", result.getName());
        assertEquals("123", result.getHouseNumber());
        assertEquals("Main Street", result.getStreet());
        assertEquals("Springfield", result.getCity());
        assertEquals("62701", result.getPostcode());
        assertEquals(39.7817, result.getLatitude(), 0.001);
        assertEquals(-89.6501, result.getLongitude(), 0.001);
        assertTrue(result.getScore() > 0, "Score should be positive");
    }

    @Test
    void geocodeReturnsMultipleResultsRankedByScore() {
        ResponseEntity<List<GeocodingResult>> response = restTemplate.exchange(
                "/geocode?q={q}", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {}, "Street");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<GeocodingResult> results = response.getBody();
        assertNotNull(results);
        assertTrue(results.size() >= 2, "Should find multiple street results");

        // Verify results are ranked by score (descending)
        for (int i = 1; i < results.size(); i++) {
            assertTrue(results.get(i - 1).getScore() >= results.get(i).getScore(),
                    "Results should be ordered by descending score");
        }
    }

    @Test
    void geocodeMissingQueryParamReturns400() {
        ResponseEntity<String> response = restTemplate.getForEntity("/geocode", String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void importRejectsPathTraversal() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/import?path={path}", null, String.class, "../../etc/passwd.pbf");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void importRejectsNonPbfFiles() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/import?path={path}", null, String.class, "data.csv");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
