package com.geocoda.service;

import com.geocoda.config.GeocodingConfig;
import com.geocoda.model.GeocodingResult;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the Apache Lucene index for geocoding data.
 * Provides methods to add address documents and to search the index
 * with fuzzy matching enabled.
 */
@Service
public class LuceneIndexService {

    private static final Logger logger = LoggerFactory.getLogger(LuceneIndexService.class);

    public static final String FIELD_NAME = "name";
    public static final String FIELD_HOUSE_NUMBER = "house_number";
    public static final String FIELD_STREET = "street";
    public static final String FIELD_CITY = "city";
    public static final String FIELD_POSTCODE = "postcode";
    public static final String FIELD_LAT = "lat";
    public static final String FIELD_LON = "lon";

    private static final String[] SEARCH_FIELDS = {
            FIELD_NAME, FIELD_STREET, FIELD_CITY, FIELD_POSTCODE, FIELD_HOUSE_NUMBER
    };

    private final GeocodingConfig config;
    private final Analyzer analyzer;
    private Directory directory;
    private IndexWriter indexWriter;

    public LuceneIndexService(GeocodingConfig config) {
        this.config = config;
        this.analyzer = new StandardAnalyzer();
    }

    @PostConstruct
    public void init() throws IOException {
        Path indexPath = Paths.get(config.getIndexDir());
        Files.createDirectories(indexPath);
        directory = FSDirectory.open(indexPath);
        IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
        writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        indexWriter = new IndexWriter(directory, writerConfig);
        logger.info("Lucene index opened at {}", indexPath.toAbsolutePath());
    }

    @PreDestroy
    public void close() throws IOException {
        if (indexWriter != null) {
            indexWriter.close();
        }
        if (directory != null) {
            directory.close();
        }
    }

    /**
     * Adds a single address document to the Lucene index.
     */
    public void addDocument(String name, String houseNumber, String street,
                            String city, String postcode,
                            double lat, double lon) throws IOException {
        Document doc = new Document();
        if (name != null) {
            doc.add(new TextField(FIELD_NAME, name, Field.Store.YES));
        }
        if (houseNumber != null) {
            doc.add(new StringField(FIELD_HOUSE_NUMBER, houseNumber, Field.Store.YES));
        }
        if (street != null) {
            doc.add(new TextField(FIELD_STREET, street, Field.Store.YES));
        }
        if (city != null) {
            doc.add(new TextField(FIELD_CITY, city, Field.Store.YES));
        }
        if (postcode != null) {
            doc.add(new StringField(FIELD_POSTCODE, postcode, Field.Store.YES));
        }
        doc.add(new StoredField(FIELD_LAT, lat));
        doc.add(new StoredField(FIELD_LON, lon));
        indexWriter.addDocument(doc);
    }

    /**
     * Commits any pending documents to the index.
     */
    public void commit() throws IOException {
        indexWriter.commit();
    }

    /**
     * Returns the number of documents currently in the index.
     */
    public int getDocumentCount() {
        return (int) indexWriter.getDocStats().maxDoc;
    }

    /**
     * Searches the index using a query string with fuzzy matching.
     * Each term in the query is appended with a tilde (~) to enable fuzziness.
     */
    public List<GeocodingResult> search(String queryString, int maxResults) throws IOException {
        if (queryString == null || queryString.isBlank()) {
            return List.of();
        }

        try (DirectoryReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            MultiFieldQueryParser parser = new MultiFieldQueryParser(SEARCH_FIELDS, analyzer);
            parser.setDefaultOperator(MultiFieldQueryParser.Operator.OR);

            String fuzzyQuery = buildFuzzyQuery(queryString);
            Query query;
            try {
                query = parser.parse(fuzzyQuery);
            } catch (ParseException e) {
                logger.warn("Failed to parse query '{}', falling back to escaped query", queryString, e);
                try {
                    query = parser.parse(MultiFieldQueryParser.escape(queryString));
                } catch (ParseException ex) {
                    logger.error("Failed to parse escaped query", ex);
                    return List.of();
                }
            }

            TopDocs topDocs = searcher.search(query, maxResults);
            List<GeocodingResult> results = new ArrayList<>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = searcher.storedFields().document(scoreDoc.doc);
                results.add(documentToResult(doc, scoreDoc.score));
            }
            return results;
        }
    }

    /**
     * Appends fuzzy matching operator (~) to each term in the query string.
     */
    String buildFuzzyQuery(String queryString) {
        String[] terms = queryString.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String term : terms) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(term).append("~");
        }
        return sb.toString();
    }

    private GeocodingResult documentToResult(Document doc, float score) {
        GeocodingResult result = new GeocodingResult();
        result.setName(doc.get(FIELD_NAME));
        result.setHouseNumber(doc.get(FIELD_HOUSE_NUMBER));
        result.setStreet(doc.get(FIELD_STREET));
        result.setCity(doc.get(FIELD_CITY));
        result.setPostcode(doc.get(FIELD_POSTCODE));

        StoredField latField = (StoredField) doc.getField(FIELD_LAT);
        StoredField lonField = (StoredField) doc.getField(FIELD_LON);
        if (latField != null) {
            result.setLatitude(latField.numericValue().doubleValue());
        }
        if (lonField != null) {
            result.setLongitude(lonField.numericValue().doubleValue());
        }
        result.setScore(score);
        return result;
    }
}
