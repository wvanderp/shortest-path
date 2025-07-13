package shortestpath.pathfinder;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import net.runelite.api.Client;
import shortestpath.ShortestPathConfig;

public class PathfinderConfig {

    private final List<Integer> filteredTargets = new ArrayList<>(4);

    private final RoutingConfig routingConfig;
    public final PlayerInformation playerInformation;
    public final DataManager dataManager;
    public final DestinationManager destinationManager;

    public PathfinderConfig(Client client, ShortestPathConfig config) {
        this.routingConfig = new RoutingConfig(config);
        this.playerInformation = new PlayerInformation(client, routingConfig);
        this.dataManager = new DataManager(client, playerInformation, routingConfig);
        this.destinationManager = new DestinationManager(routingConfig);
    }

    public CollisionMap getMap() {
        return dataManager.getMap();
    }

    public void refresh() {
        dataManager.refresh();
        playerInformation.refresh();
        routingConfig.refresh();
        destinationManager.refresh();
    }

    public long getCalculationCutoffMillis() {
        return routingConfig.calculationCutoffMillis;
    }

    /**
     * Changes to the config might have invalidated some locations, e.g. those in
     * the wilderness
     */
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

    public boolean avoidWilderness(int packedPosition, int packedNeighborPosition, boolean targetInWilderness) {
        return this.routingConfig.avoidWilderness && !targetInWilderness
                && !WildernessChecker.isInWilderness(packedPosition)
                && WildernessChecker.isInWilderness(packedNeighborPosition);
    }

}
