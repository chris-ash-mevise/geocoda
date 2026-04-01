package com.geocoda.service;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.pbf2.v0_6.PbfReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses OpenStreetMap PBF files using the Visitor Pattern and feeds
 * address-bearing nodes into the Lucene index.
 */
@Service
public class OsmPbfParser {

    private static final Logger logger = LoggerFactory.getLogger(OsmPbfParser.class);

    private final LuceneIndexService indexService;

    public OsmPbfParser(LuceneIndexService indexService) {
        this.indexService = indexService;
    }

    /**
     * Streams an OSM PBF file and indexes every node that carries at least
     * an {@code addr:street} tag.
     *
     * @param pbfFile the PBF file to parse
     * @return the number of nodes indexed
     */
    public long parse(File pbfFile) throws IOException {
        if (!pbfFile.exists()) {
            throw new IOException("PBF file not found: " + pbfFile.getAbsolutePath());
        }

        logger.info("Starting PBF import from {}", pbfFile.getAbsolutePath());
        long[] count = {0};

        PbfReader reader = new PbfReader(pbfFile, 1);
        reader.setSink(new Sink() {
            @Override
            public void process(EntityContainer entityContainer) {
                if (entityContainer instanceof NodeContainer nodeContainer) {
                    Node node = nodeContainer.getEntity();
                    Map<String, String> tags = tagsToMap(node);

                    if (tags.containsKey("addr:street")) {
                        try {
                            indexService.addDocument(
                                    tags.get("name"),
                                    tags.get("addr:housenumber"),
                                    tags.get("addr:street"),
                                    tags.containsKey("addr:city")
                                            ? tags.get("addr:city")
                                            : tags.get("addr:town"),
                                    tags.get("addr:postcode"),
                                    node.getLatitude(),
                                    node.getLongitude()
                            );
                            count[0]++;
                            if (count[0] % 50_000 == 0) {
                                logger.info("Indexed {} address nodes so far", count[0]);
                            }
                        } catch (IOException e) {
                            logger.error("Failed to index node {}", node.getId(), e);
                        }
                    }
                }
            }

            @Override
            public void initialize(Map<String, Object> metaData) {
                // no-op
            }

            @Override
            public void complete() {
                // no-op
            }

            @Override
            public void close() {
                // no-op
            }
        });

        reader.run();
        indexService.commit();
        logger.info("PBF import complete. Indexed {} address nodes.", count[0]);
        return count[0];
    }

    private static Map<String, String> tagsToMap(Node node) {
        Map<String, String> map = new HashMap<>();
        for (Tag tag : node.getTags()) {
            map.put(tag.getKey(), tag.getValue());
        }
        return map;
    }
}
