package shortestpath.pathfinder;

import java.util.*;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import shortestpath.*;

public class DataManager {
    private final SplitFlagMap mapData;
    private final ThreadLocal<CollisionMap> map;
    private final Map<Integer, Set<Transport>> allTransports;
    private final Set<Transport> usableTeleports;
    private final Map<String, Set<Integer>> allDestinations;
    private final Map<String, Set<Integer>> filteredDestinations;
    private final List<Integer> filteredTargets = new ArrayList<>(4);
    private Map<String, Set<Integer>> destinations;
    private final Map<Integer, Set<Transport>> transports;
    private final PrimitiveIntHashMap<Set<Transport>> transportsPacked;
    private final Client client;
    private final PlayerInformation playerInformation;
    private final RoutingConfig routingConfig;

    public DataManager(Client client, ShortestPathConfig config, PlayerInformation playerInformation,
            RoutingConfig routingConfig) {
        this.client = client;
        this.mapData = SplitFlagMap.fromResources();
        this.map = ThreadLocal.withInitial(() -> new CollisionMap(mapData));
        this.allTransports = Transport.loadAllFromResources();
        this.usableTeleports = new HashSet<>(allTransports.size() / 20);
        this.transports = new HashMap<>(allTransports.size() / 2);
        this.transportsPacked = new PrimitiveIntHashMap<>(allTransports.size() / 2);
        this.allDestinations = Destination.loadAllFromResources();
        this.filteredDestinations = filterDestinations(allDestinations);
        this.destinations = allDestinations;
        this.playerInformation = playerInformation;
        this.routingConfig = routingConfig;
    }

    public CollisionMap getMap() {
        return map.get();
    }

    public boolean hasDestination(String destinationType) {
        return destinations.hasDestination(destinationType);
    }

    public Set<Integer> getDestinations(String destinationType) {
        return destinations.get(destinationType);
    }

    public void refresh() {
        refreshTransports();
        refreshDestinations();
    }

    /**
     * Specialized method for only updating player-held item and spell transports
     */
    public void refreshTeleports(int packedLocation, int wildernessLevel) {
        Set<Transport> usableWildyTeleports = new HashSet<>(usableTeleports.size());

        for (Transport teleport : usableTeleports) {
            if (wildernessLevel <= teleport.getMaxWildernessLevel()) {
                usableWildyTeleports.add(teleport);
            }
        }

        if (!usableWildyTeleports.isEmpty()) {
            transports.put(packedLocation, usableWildyTeleports);
            transportsPacked.put(packedLocation, usableWildyTeleports);
        }
    }

    public void filterLocations(Set<Integer> locations, boolean canReviveFiltered, RoutingConfig routingConfig) {
        if (routingConfig.avoidWilderness) {
            locations.removeIf(location -> {
                boolean inWilderness = WildernessChecker.isInWilderness(location);
                if (inWilderness) {
                    filteredTargets.add(location);
                }
                return inWilderness;
            });
            if (locations.isEmpty()) {
                locations.addAll(filteredTargets);
                filteredTargets.clear();
            }
        } else if (canReviveFiltered) {
            locations.addAll(filteredTargets);
            filteredTargets.clear();
        }
    }

    private void refreshDestinations(RoutingConfig routingConfig) {
        destinations = routingConfig.avoidWilderness ? filteredDestinations : allDestinations;
    }

    private Map<String, Set<Integer>> filterDestinations(Map<String, Set<Integer>> allDestinations) {
        Map<String, Set<Integer>> filteredDestinations = new HashMap<>(allDestinations.size());
        for (Map.Entry<String, Set<Integer>> entry : allDestinations.entrySet()) {
            String destinationType = entry.getKey();
            Set<Integer> usableDestinations = new HashSet<>(entry.getValue().size());
            boolean isDifferent = false;
            for (Integer destination : entry.getValue()) {
                if (!WildernessChecker.isInWilderness(destination)) {
                    usableDestinations.add(destination);
                    isDifferent = true;
                }
            }
            if (!usableDestinations.isEmpty()) {
                filteredDestinations.put(destinationType, isDifferent ? usableDestinations : entry.getValue());
            }
        }
        return filteredDestinations;
    }

    private void refreshTransports() {
        if (!Thread.currentThread().equals(client.getClientThread())) {
            return; // Has to run on the client thread; data will be refreshed when path finding
                    // commences
        }

        routingConfig.useFairyRings &= !QuestState.NOT_STARTED.equals(playerInformation.getQuestState(Quest.FAIRYTALE_II__CURE_A_QUEEN));
        routingConfig.useGnomeGliders &= QuestState.FINISHED.equals(playerInformation.getQuestState(Quest.THE_GRAND_TREE));
        routingConfig.useSpiritTrees &= QuestState.FINISHED.equals(playerInformation.getQuestState(Quest.TREE_GNOME_VILLAGE));

        transports.clear();
        transportsPacked.clear();
        usableTeleports.clear();
        for (Map.Entry<Integer, Set<Transport>> entry : allTransports.entrySet()) {
            int point = entry.getKey();
            Set<Transport> usableTransports = new HashSet<>(entry.getValue().size());
            for (Transport transport : entry.getValue()) {
                for (Quest quest : transport.getQuests()) {
                    try {
                        playerInformation.questStates.put(quest, playerInformation.getQuestState(quest));
                    } catch (NullPointerException ignored) {
                    }
                }

                for (TransportVarbit varbitRequirement : transport.getVarbits()) {
                    playerInformation.varbitValues.put(varbitRequirement.getId(), client.getVarbitValue(varbitRequirement.getId()));
                }
                for (TransportVarPlayer varPlayerRequirement : transport.getVarPlayers()) {
                    playerInformation.varPlayerValues.put(varPlayerRequirement.getId(),
                            client.getVarpValue(varPlayerRequirement.getId()));
                }

                if (playerInformation.useTransport(transport) && playerInformation.hasRequiredItems(transport)) {
                    if (point == WorldPointUtil.UNDEFINED) {
                        usableTeleports.add(transport);
                    } else {
                        usableTransports.add(transport);
                    }
                }
            }

            if (point != WorldPointUtil.UNDEFINED && !usableTransports.isEmpty()) {
                transports.put(point, usableTransports);
                transportsPacked.put(point, usableTransports);
            }
        }
    }

}
