package com.geocoda.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the geocoding system.
 */
@Configuration
@ConfigurationProperties(prefix = "geocoda")
public class GeocodingConfig {

    /** Filesystem path to the Lucene index directory. */
    private String indexDir = "index";

    /** Filesystem path to the OSM PBF data file to import. */
    private String pbfFilePath = "";

    /** Allowed base directory for PBF file imports. */
    private String dataDir = "data";

    /** Maximum number of search results to return. */
    private int maxResults = 10;

    public String getIndexDir() {
        return indexDir;
    }

    public void setIndexDir(String indexDir) {
        this.indexDir = indexDir;
    }

    public String getPbfFilePath() {
        return pbfFilePath;
    }

    public void setPbfFilePath(String pbfFilePath) {
        this.pbfFilePath = pbfFilePath;
    }

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }
}
