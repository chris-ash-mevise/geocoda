package com.geocoda.service;

import com.geocoda.config.GeocodingConfig;
import com.geocoda.model.GeocodingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * High-level geocoding service that orchestrates PBF parsing,
 * Lucene indexing, and search.
 */
@Service
public class GeocodingService {

    private static final Logger logger = LoggerFactory.getLogger(GeocodingService.class);

    private final GeocodingConfig config;
    private final LuceneIndexService indexService;
    private final OsmPbfParser parser;

    public GeocodingService(GeocodingConfig config,
                            LuceneIndexService indexService,
                            OsmPbfParser parser) {
        this.config = config;
        this.indexService = indexService;
        this.parser = parser;
    }

    /**
     * On application startup, imports the configured PBF file if one is set.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        String pbfPath = config.getPbfFilePath();
        if (pbfPath != null && !pbfPath.isBlank()) {
            try {
                importPbf(pbfPath);
            } catch (IOException e) {
                logger.error("Failed to import PBF file on startup: {}", pbfPath, e);
            }
        } else {
            logger.info("No PBF file configured (geocoda.pbf-file-path). "
                    + "Start the server and POST to /import to load data.");
        }
    }

    /**
     * Imports a PBF file into the Lucene index.
     * The file path is validated to prevent path traversal attacks.
     *
     * @param pbfFilePath path to the .osm.pbf file
     * @return the number of address nodes indexed
     */
    public long importPbf(String pbfFilePath) throws IOException {
        Path normalized = Path.of(pbfFilePath).toAbsolutePath().normalize();
        String fileName = normalized.getFileName().toString();
        if (!fileName.endsWith(".pbf")) {
            throw new IOException("Only .pbf files are accepted: " + fileName);
        }
        return parser.parse(normalized.toFile());
    }

    /**
     * Searches the index for the given query string.
     */
    public List<GeocodingResult> geocode(String query) throws IOException {
        return indexService.search(query, config.getMaxResults());
    }
}
