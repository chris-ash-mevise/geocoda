package com.geocoda.service;

import com.geocoda.config.GeocodingConfig;
import com.geocoda.model.GeocodingResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LuceneIndexServiceTest {

    @TempDir
    Path tempDir;

    private LuceneIndexService service;

    @BeforeEach
    void setUp() throws Exception {
        GeocodingConfig config = new GeocodingConfig();
        config.setIndexDir(tempDir.resolve("test-index").toString());
        service = new LuceneIndexService(config);
        service.init();
    }

    @AfterEach
    void tearDown() throws Exception {
        service.close();
    }

    @Test
    void addDocumentAndSearchByStreet() throws Exception {
        service.addDocument("Acme Corp", "123", "Main Street", "Springfield", "62701",
                39.7817, -89.6501);
        service.commit();

        List<GeocodingResult> results = service.search("Main Street", 10);

        assertFalse(results.isEmpty());
        GeocodingResult first = results.get(0);
        assertEquals("Main Street", first.getStreet());
        assertEquals("123", first.getHouseNumber());
        assertEquals("Springfield", first.getCity());
    }

    @Test
    void fuzzyMatchHandlesTypos() throws Exception {
        service.addDocument(null, "456", "Broadway Avenue", "New York", "10001",
                40.7484, -73.9967);
        service.commit();

        // "Brodway" is a typo for "Broadway"
        List<GeocodingResult> results = service.search("Brodway", 10);

        assertFalse(results.isEmpty(), "Fuzzy matching should find 'Broadway' from 'Brodway'");
        assertEquals("Broadway Avenue", results.get(0).getStreet());
    }

    @Test
    void searchByCity() throws Exception {
        service.addDocument(null, "1", "Elm Street", "Portland", "97201",
                45.5152, -122.6784);
        service.commit();

        List<GeocodingResult> results = service.search("Portland", 10);

        assertFalse(results.isEmpty());
        assertEquals("Portland", results.get(0).getCity());
    }

    @Test
    void emptyQueryReturnsNoResults() throws Exception {
        service.addDocument(null, "1", "Oak Lane", "Austin", "73301",
                30.2672, -97.7431);
        service.commit();

        assertTrue(service.search("", 10).isEmpty());
        assertTrue(service.search(null, 10).isEmpty());
        assertTrue(service.search("   ", 10).isEmpty());
    }

    @Test
    void coordinatesAreStored() throws Exception {
        service.addDocument("Test Place", "10", "River Road", "Denver", "80201",
                39.7392, -104.9903);
        service.commit();

        List<GeocodingResult> results = service.search("River Road", 10);

        assertFalse(results.isEmpty());
        GeocodingResult result = results.get(0);
        assertEquals(39.7392, result.getLatitude(), 0.001);
        assertEquals(-104.9903, result.getLongitude(), 0.001);
    }

    @Test
    void buildFuzzyQueryAppendsTilde() {
        String fuzzy = service.buildFuzzyQuery("Main Street Springfield");
        assertEquals("Main~ Street~ Springfield~", fuzzy);
    }

    @Test
    void multipleDocumentsReturnedRankedByScore() throws Exception {
        service.addDocument(null, "100", "Main Street", "Springfield", "62701",
                39.7817, -89.6501);
        service.addDocument(null, "200", "Main Avenue", "Springfield", "62702",
                39.7900, -89.6400);
        service.addDocument(null, "300", "Oak Street", "Denver", "80201",
                39.7392, -104.9903);
        service.commit();

        List<GeocodingResult> results = service.search("Main Springfield", 10);

        assertNotNull(results);
        assertTrue(results.size() >= 2, "Should find at least 2 results for 'Main Springfield'");
        // Scores should be in descending order
        for (int i = 1; i < results.size(); i++) {
            assertTrue(results.get(i - 1).getScore() >= results.get(i).getScore());
        }
    }
}
