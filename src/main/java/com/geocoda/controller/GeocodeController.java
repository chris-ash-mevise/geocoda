package com.geocoda.controller;

import com.geocoda.model.GeocodingResult;
import com.geocoda.service.GeocodingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * REST controller exposing the geocoding API.
 */
@RestController
public class GeocodeController {

    private final GeocodingService geocodingService;

    public GeocodeController(GeocodingService geocodingService) {
        this.geocodingService = geocodingService;
    }

    /**
     * Geocode endpoint. Accepts a free-text query and returns matching
     * addresses with coordinates.
     *
     * @param q the search query (e.g. "123 Main St")
     * @return list of geocoding results
     */
    @GetMapping("/geocode")
    public ResponseEntity<List<GeocodingResult>> geocode(@RequestParam String q) throws IOException {
        List<GeocodingResult> results = geocodingService.geocode(q);
        return ResponseEntity.ok(results);
    }

    /**
     * Triggers import of a PBF file into the index.
     *
     * @param path filesystem path to the .osm.pbf file
     * @return summary of the import
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importPbf(@RequestParam String path) throws IOException {
        long count = geocodingService.importPbf(path);
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "indexedNodes", count
        ));
    }
}
