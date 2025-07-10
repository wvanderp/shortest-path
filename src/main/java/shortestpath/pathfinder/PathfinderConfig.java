package shortestpath.pathfinder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.EnumComposition;
import net.runelite.api.EnumID;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.Varbits;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import shortestpath.TeleportationItem;
import shortestpath.ShortestPathConfig;
import shortestpath.ShortestPathPlugin;
import shortestpath.Destination;
import shortestpath.PrimitiveIntHashMap;
import shortestpath.Transport;
import shortestpath.TransportItems;
import shortestpath.TransportType;
import shortestpath.TransportVarbit;
import shortestpath.TransportVarPlayer;
import shortestpath.WorldPointUtil;
import static shortestpath.TransportType.AGILITY_SHORTCUT;
import static shortestpath.TransportType.GRAPPLE_SHORTCUT;
import static shortestpath.TransportType.BOAT;
import static shortestpath.TransportType.CANOE;
import static shortestpath.TransportType.CHARTER_SHIP;
import static shortestpath.TransportType.SHIP;
import static shortestpath.TransportType.FAIRY_RING;
import static shortestpath.TransportType.GNOME_GLIDER;
import static shortestpath.TransportType.HOT_AIR_BALLOON;
import static shortestpath.TransportType.MINECART;
import static shortestpath.TransportType.QUETZAL;
import static shortestpath.TransportType.SPIRIT_TREE;
import static shortestpath.TransportType.TELEPORTATION_LEVER;
import static shortestpath.TransportType.TELEPORTATION_MINIGAME;
import static shortestpath.TransportType.TELEPORTATION_PORTAL;
import static shortestpath.TransportType.TELEPORTATION_ITEM;
import static shortestpath.TransportType.TELEPORTATION_SPELL;
import static shortestpath.TransportType.WILDERNESS_OBELISK;

public class PathfinderConfig {

    private final SplitFlagMap mapData;
    /** All transports by origin. The WorldPointUtil.UNDEFINED key is used for transports centered on the player. */
    private final Map<Integer, Set<Transport>> allTransports;
    private final Map<String, Set<Integer>> allDestinations;
    private final List<Integer> filteredTargets = new ArrayList<>(4);

    @Getter
    private final Map<Integer, Set<Transport>> transports;
    // Copy of transports with packed positions for the hotpath; lists are not copied and are the same reference in both maps
    @Getter
    private final PrimitiveIntHashMap<Set<Transport>> transportsPacked;
    /** Reference that points to either allDestinations or filteredDestinations */
    private Map<String, Set<Integer>> destinations;



    private final RoutingConfig routingConfig;
    private final PlayerInformation playerInformation;
    private final DataManager dataManager;

    public PathfinderConfig(Client client, ShortestPathConfig config) {
        this.mapData = SplitFlagMap.fromResources();
        this.allTransports = Transport.loadAllFromResources();
        this.transports = new HashMap<>(allTransports.size() / 2);
        this.transportsPacked = new PrimitiveIntHashMap<>(allTransports.size() / 2);
        this.allDestinations = Destination.loadAllFromResources();
        this.destinations = allDestinations;

        this.routingConfig = new RoutingConfig(config);
        this.playerInformation = new PlayerInformation(client, routingConfig);
        this.dataManager = new DataManager(client, config, playerInformation, routingConfig);
    }

    public CollisionMap getMap() {
        return dataManager.getMap();
    }

    public boolean hasDestination(String destinationType) {
        return dataManager.hasDestination(destinationType);
    }

    public Set<Integer> getDestinations(String destinationType) {
        return dataManager.getDestinations(destinationType);
    }

    public void refresh() {
        dataManager.refresh();
        playerInformation.refresh();
        routingConfig.refresh();
    }




    /** Changes to the config might have invalidated some locations, e.g. those in the wilderness */
    public void filterLocations(Set<Integer> locations, boolean canReviveFiltered) {
        if (this.routingConfig.avoidWilderness) {
            locations.removeIf(location -> {
                boolean inWilderness = WildernessChecker.isInWilderness(location);
                if (inWilderness) {
                    filteredTargets.add(location);
                }
                return inWilderness;
            });
            // If we ended up with no valid locations we re-include the filtered locations
            if (locations.isEmpty()) {
                locations.addAll(filteredTargets);
                filteredTargets.clear();
            }
        } else if (canReviveFiltered) { // Re-include previously filtered locations
            locations.addAll(filteredTargets);
            filteredTargets.clear();
        }
    }

    private Map<String, Set<Integer>> filterDestinations(Map<String, Set<Integer>> allDestinations) {
        Map<String, Set<Integer>> filteredDestinations = new HashMap<>(allDestinations.size());
        for (Map.Entry<String, Set<Integer>> entry : allDestinations.entrySet()) {
            String destinationType = entry.getKey();
            Set<Integer> usableDestinations = new HashSet<>(entry.getValue().size());
            boolean isDifferent = false;
            for (Integer destination : entry.getValue()) {
                // We filter based on whether the destination is inside or outside wilderness
                if (!WildernessChecker.isInWilderness(destination)) {
                    usableDestinations.add(destination);
                    isDifferent = true;
                }
            }
            // If all destinations of a destination type have been filtered away then we don't add the entry
            if (!usableDestinations.isEmpty()) {
                // If no destinations of a destination type have been filtered away then we re-use the same set reference
                filteredDestinations.put(destinationType, isDifferent ? usableDestinations : entry.getValue());
            }
        }
        return filteredDestinations;
    }


    public boolean avoidWilderness(int packedPosition, int packedNeighborPosition, boolean targetInWilderness) {
        return this.routingConfig.avoidWilderness && !targetInWilderness
            && !WildernessChecker.isInWilderness(packedPosition) && WildernessChecker.isInWilderness(packedNeighborPosition);
    }


  }
