package shortestpath;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

/**
 * Handles loading transport data from TSV resources and creating Transport objects.
 * Separates the concern of file loading from the Transport class itself.
 */
@Slf4j
public class TransportLoader {
    
    /** A location placeholder different from null to use for permutation transports */
    private static final int LOCATION_PERMUTATION = WorldPointUtil.packWorldPoint(-1, -1, 1);

    /**
     * Configuration for loading a specific transport type.
     */
    public static class TransportConfig {
        private final String resourcePath;
        private final TransportType transportType;
        private final int radiusThreshold;

        public TransportConfig(String resourcePath, TransportType transportType) {
            this(resourcePath, transportType, 0);
        }

        public TransportConfig(String resourcePath, TransportType transportType, int radiusThreshold) {
            this.resourcePath = resourcePath;
            this.transportType = transportType;
            this.radiusThreshold = radiusThreshold;
        }

        public String getResourcePath() {
            return resourcePath;
        }

        public TransportType getTransportType() {
            return transportType;
        }

        public int getRadiusThreshold() {
            return radiusThreshold;
        }
    }

    /**
     * Loads all transport data from the default resource configurations.
     * 
     * @return Map of packed world points to sets of transports
     */
    public static HashMap<Integer, Set<Transport>> loadAllFromResources() {
        TransportConfig[] configs = {
            new TransportConfig("/transports/transports.tsv", TransportType.TRANSPORT),
            new TransportConfig("/transports/agility_shortcuts.tsv", TransportType.AGILITY_SHORTCUT),
            new TransportConfig("/transports/boats.tsv", TransportType.BOAT),
            new TransportConfig("/transports/canoes.tsv", TransportType.CANOE),
            new TransportConfig("/transports/charter_ships.tsv", TransportType.CHARTER_SHIP),
            new TransportConfig("/transports/ships.tsv", TransportType.SHIP),
            new TransportConfig("/transports/fairy_rings.tsv", TransportType.FAIRY_RING),
            new TransportConfig("/transports/gnome_gliders.tsv", TransportType.GNOME_GLIDER, 6),
            new TransportConfig("/transports/hot_air_balloons.tsv", TransportType.HOT_AIR_BALLOON, 7),
            new TransportConfig("/transports/magic_mushtrees.tsv", TransportType.MAGIC_MUSHTREE, 5),
            new TransportConfig("/transports/minecarts.tsv", TransportType.MINECART),
            new TransportConfig("/transports/quetzals.tsv", TransportType.QUETZAL),
            new TransportConfig("/transports/spirit_trees.tsv", TransportType.SPIRIT_TREE, 5),
            new TransportConfig("/transports/teleportation_items.tsv", TransportType.TELEPORTATION_ITEM),
            new TransportConfig("/transports/teleportation_boxes.tsv", TransportType.TELEPORTATION_BOX),
            new TransportConfig("/transports/teleportation_levers.tsv", TransportType.TELEPORTATION_LEVER),
            new TransportConfig("/transports/teleportation_minigames.tsv", TransportType.TELEPORTATION_MINIGAME),
            new TransportConfig("/transports/teleportation_portals.tsv", TransportType.TELEPORTATION_PORTAL),
            new TransportConfig("/transports/teleportation_spells.tsv", TransportType.TELEPORTATION_SPELL),
            new TransportConfig("/transports/wilderness_obelisks.tsv", TransportType.WILDERNESS_OBELISK)
        };

        return loadFromConfigs(configs);
    }

    /**
     * Loads transport data from an array of configurations.
     * 
     * @param configs Array of transport configurations to load
     * @return Map of packed world points to sets of transports
     */
    public static HashMap<Integer, Set<Transport>> loadFromConfigs(TransportConfig[] configs) {
        HashMap<Integer, Set<Transport>> transports = new HashMap<>();
        
        for (TransportConfig config : configs) {
            try {
                addTransports(transports, config);
            } catch (Exception e) {
                log.error("Failed to load transports from {}: {}", config.getResourcePath(), e.getMessage(), e);
                // Continue loading other configs even if one fails
            }
        }
        
        return transports;
    }

    /**
     * Loads transport data from a single configuration and adds it to the provided map.
     * 
     * @param transports The map to add transports to
     * @param config The configuration specifying what to load
     */
    public static void addTransports(Map<Integer, Set<Transport>> transports, TransportConfig config) {
        try {
            // Load and parse the TSV data
            byte[] bytes = Util.readAllBytes(ShortestPathPlugin.class.getResourceAsStream(config.getResourcePath()));
            TsvParser.TsvData tsvData = TsvParser.parse(bytes);
            
            // Create Transport objects from parsed data
            Set<Transport> newTransports = createTransportsFromTsvData(tsvData, config.getTransportType());
            
            // Process transport relationships and add to map
            processTransportRelationships(transports, newTransports, config.getRadiusThreshold());
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to read transport resource: " + config.getResourcePath(), e);
        } catch (TsvParser.TsvParseException e) {
            throw new RuntimeException("Failed to parse transport TSV: " + config.getResourcePath(), e);
        }
    }

    /**
     * Creates Transport objects from parsed TSV data.
     */
    private static Set<Transport> createTransportsFromTsvData(TsvParser.TsvData tsvData, TransportType transportType) {
        Set<Transport> transports = new HashSet<>();
        
        for (Map<String, String> row : tsvData.getRows()) {
            try {
                Transport transport = new Transport(row, transportType);
                transports.add(transport);
            } catch (Exception e) {
                log.error("Failed to create transport from row {}: {}", row, e.getMessage());
                // Continue processing other rows
            }
        }
        
        return transports;
    }

    /**
     * Processes transport relationships and adds them to the transport map.
     * This handles the complex logic for transport permutations, teleports, and two-way transports.
     */
    private static void processTransportRelationships(Map<Integer, Set<Transport>> transports, 
                                                    Set<Transport> newTransports, 
                                                    int radiusThreshold) {
        /*
         * A transport with origin A and destination B is one-way and must
         * be duplicated as origin B and destination A to become two-way.
         * Example: key-locked doors
         * 
         * A transport with origin A and a missing destination is one-way,
         * but can go from origin A to all destinations with a missing origin.
         * Example: fairy ring AIQ -> <blank>
         * 
         * A transport with a missing origin and destination B is one-way,
         * but can go from all origins with a missing destination to destination B.
         * Example: fairy ring <blank> -> AIQ
         * 
         * Identical transports from origin A to destination A are skipped, and
         * non-identical transports from origin A to destination A can be skipped
         * by specifying a radius threshold to ignore almost identical coordinates.
         * Example: fairy ring AIQ -> AIQ
         */
        Set<Transport> transportOrigins = new HashSet<>();
        Set<Transport> transportDestinations = new HashSet<>();
        
        for (Transport transport : newTransports) {
            int origin = transport.getOrigin();
            int destination = transport.getDestination();
            
            // Logic to determine ordinary transport vs teleport vs permutation (e.g. fairy ring)
            if ((origin == Transport.UNDEFINED_ORIGIN && destination == Transport.UNDEFINED_DESTINATION)
                || (origin == LOCATION_PERMUTATION && destination == LOCATION_PERMUTATION)) {
                continue;
            } else if (origin != LOCATION_PERMUTATION && origin != Transport.UNDEFINED_ORIGIN
                && destination == LOCATION_PERMUTATION) {
                transportOrigins.add(transport);
            } else if (origin == LOCATION_PERMUTATION
                && destination != LOCATION_PERMUTATION && destination != Transport.UNDEFINED_DESTINATION) {
                transportDestinations.add(transport);
            }
            
            if (origin != LOCATION_PERMUTATION
                && destination != Transport.UNDEFINED_DESTINATION && destination != LOCATION_PERMUTATION
                && (origin == Transport.UNDEFINED_ORIGIN || origin != destination)) {
                transports.computeIfAbsent(origin, k -> new HashSet<>()).add(transport);
            }
        }
        
        // Create permutation transports
        for (Transport origin : transportOrigins) {
            for (Transport destination : transportDestinations) {
                // The radius threshold prevents transport permutations from including (almost) same origin and destination
                if (WorldPointUtil.distanceBetween2D(origin.getOrigin(), destination.getDestination()) > radiusThreshold) {
                    transports.computeIfAbsent(origin.getOrigin(), k -> new HashSet<>())
                        .add(new Transport(origin, destination));
                }
            }
        }
    }
}