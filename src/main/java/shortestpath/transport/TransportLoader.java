package shortestpath.transport;

import shortestpath.TsvReader;
import shortestpath.WorldPointUtil;

import java.io.IOException;
import java.util.*;

public class TransportLoader {
    private static final int UNDEFINED_ORIGIN = WorldPointUtil.UNDEFINED;
    private static final int UNDEFINED_DESTINATION = WorldPointUtil.UNDEFINED;

    private static final int LOCATION_PERMUTATION = WorldPointUtil.packWorldPoint(-1, -1, 1);

    private static void addTransports(Map<Integer, Set<Transport>> transports, String path,
            TransportType transportType) {
        addTransports(transports, path, transportType, 0);
    }

    private static void addTransports(
            Map<Integer, Set<Transport>> transports,
            String path,
            TransportType transportType,
            int radiusThreshold) {
        try {
            List<Map<String, String>> rows = TsvReader.readResource(path);
            Set<Transport> newTransports = new HashSet<>();
            for (Map<String, String> fieldMap : rows) {
                Transport transport = new Transport(fieldMap, transportType);
                newTransports.add(transport);
            }

            Set<Transport> transportOrigins = new HashSet<>();
            Set<Transport> transportDestinations = new HashSet<>();
            for (Transport transport : newTransports) {
                int origin = transport.getOrigin();
                int destination = transport.getDestination();
                if ((origin == UNDEFINED_ORIGIN && destination == UNDEFINED_DESTINATION)
                        || (origin == LOCATION_PERMUTATION && destination == LOCATION_PERMUTATION)) {
                    continue;
                } else if (origin != LOCATION_PERMUTATION && origin != UNDEFINED_ORIGIN
                        && destination == LOCATION_PERMUTATION) {
                    transportOrigins.add(transport);
                } else if (origin == LOCATION_PERMUTATION
                        && destination != LOCATION_PERMUTATION && destination != UNDEFINED_DESTINATION) {
                    transportDestinations.add(transport);
                }
                if (origin != LOCATION_PERMUTATION
                        && destination != UNDEFINED_DESTINATION && destination != LOCATION_PERMUTATION
                        && (origin == UNDEFINED_ORIGIN || origin != destination)) {
                    transports.computeIfAbsent(origin, k -> new HashSet<>()).add(transport);
                }
            }
            for (Transport origin : transportOrigins) {
                for (Transport destination : transportDestinations) {
                    if (WorldPointUtil.distanceBetween2D(origin.getOrigin(),
                            destination.getDestination()) > radiusThreshold) {
                        transports.computeIfAbsent(origin.getOrigin(), k -> new HashSet<>())
                                .add(new Transport(origin, destination));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static HashMap<Integer, Set<Transport>> loadAllFromResources() {
        HashMap<Integer, Set<Transport>> transports = new HashMap<>();
        addTransports(transports, "/transports/transports.tsv", TransportType.TRANSPORT);
        addTransports(transports, "/transports/agility_shortcuts.tsv", TransportType.AGILITY_SHORTCUT);
        addTransports(transports, "/transports/boats.tsv", TransportType.BOAT);
        addTransports(transports, "/transports/canoes.tsv", TransportType.CANOE);
        addTransports(transports, "/transports/charter_ships.tsv", TransportType.CHARTER_SHIP);
        addTransports(transports, "/transports/ships.tsv", TransportType.SHIP);
        addTransports(transports, "/transports/fairy_rings.tsv", TransportType.FAIRY_RING);
        addTransports(transports, "/transports/gnome_gliders.tsv", TransportType.GNOME_GLIDER, 6);
        addTransports(transports, "/transports/hot_air_balloons.tsv", TransportType.HOT_AIR_BALLOON, 7);
        addTransports(transports, "/transports/magic_mushtrees.tsv", TransportType.MAGIC_MUSHTREE, 5);
        addTransports(transports, "/transports/minecarts.tsv", TransportType.MINECART);
        addTransports(transports, "/transports/quetzals.tsv", TransportType.QUETZAL);
        addTransports(transports, "/transports/spirit_trees.tsv", TransportType.SPIRIT_TREE, 5);
        addTransports(transports, "/transports/teleportation_items.tsv", TransportType.TELEPORTATION_ITEM);
        addTransports(transports, "/transports/teleportation_boxes.tsv", TransportType.TELEPORTATION_BOX);
        addTransports(transports, "/transports/teleportation_levers.tsv", TransportType.TELEPORTATION_LEVER);
        addTransports(transports, "/transports/teleportation_minigames.tsv", TransportType.TELEPORTATION_MINIGAME);
        addTransports(transports, "/transports/teleportation_portals.tsv", TransportType.TELEPORTATION_PORTAL);
        addTransports(transports, "/transports/teleportation_spells.tsv", TransportType.TELEPORTATION_SPELL);
        addTransports(transports, "/transports/wilderness_obelisks.tsv", TransportType.WILDERNESS_OBELISK);
        return transports;
    }
}
