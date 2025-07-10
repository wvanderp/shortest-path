package shortestpath.pathfinder;

import shortestpath.ShortestPathConfig;
import shortestpath.ShortestPathPlugin;
import shortestpath.TeleportationItem;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.GameState;
import net.runelite.api.Skill;

public class RoutingConfig {
    // Configuration and settings related to routing/pathfinding options
    // Fields will be moved here from PathfinderConfig

    private final ShortestPathConfig config;
    @Getter
    private boolean avoidWilderness;
    @Getter
    private boolean useAgilityShortcuts;
    @Getter
    private boolean useGrappleShortcuts;
    @Getter
    private boolean useBoats;
    @Getter
    private boolean useCanoes;
    @Getter
    private boolean useCharterShips;
    @Getter
    private boolean useShips;
    @Getter
    public boolean useFairyRings;
    @Getter
    public boolean useGnomeGliders;
    @Getter
    private boolean useHotAirBalloons;
    @Getter
    private boolean useMinecarts;
    @Getter
    private boolean useQuetzals;
    @Getter
    public boolean useSpiritTrees;
    @Getter
    private boolean useTeleportationLevers;
    @Getter
    private boolean useTeleportationMinigames;
    @Getter
    private boolean useTeleportationPortals;
    @Getter
    private boolean useTeleportationSpells;
    @Getter
    private boolean useWildernessObelisks;
    @Getter
    private int currencyThreshold;
    @Getter
    private TeleportationItem useTeleportationItems;
    @Getter
    private long calculationCutoffMillis;



    public RoutingConfig(ShortestPathConfig config) {
        this.config = config;
        refresh();
    }

    public void refresh() {
        calculationCutoffMillis = config.calculationCutoff() * Constants.GAME_TICK_LENGTH;
        avoidWilderness = ShortestPathPlugin.override("avoidWilderness", config.avoidWilderness());
        useAgilityShortcuts = ShortestPathPlugin.override("useAgilityShortcuts", config.useAgilityShortcuts());
        useGrappleShortcuts = ShortestPathPlugin.override("useGrappleShortcuts", config.useGrappleShortcuts());
        useBoats = ShortestPathPlugin.override("useBoats", config.useBoats());
        useCanoes = ShortestPathPlugin.override("useCanoes", config.useCanoes());
        useCharterShips = ShortestPathPlugin.override("useCharterShips", config.useCharterShips());
        useShips = ShortestPathPlugin.override("useShips", config.useShips());
        useFairyRings = ShortestPathPlugin.override("useFairyRings", config.useFairyRings());
        useGnomeGliders = ShortestPathPlugin.override("useGnomeGliders", config.useGnomeGliders());
        useHotAirBalloons = ShortestPathPlugin.override("useHotAirBalloons", config.useHotAirBalloons());
        useMinecarts = ShortestPathPlugin.override("useMinecarts", config.useMinecarts());
        useQuetzals = ShortestPathPlugin.override("useQuetzals", config.useQuetzals());
        useSpiritTrees = ShortestPathPlugin.override("useSpiritTrees", config.useSpiritTrees());
        useTeleportationItems = ShortestPathPlugin.override("useTeleportationItems", config.useTeleportationItems());
        useTeleportationLevers = ShortestPathPlugin.override("useTeleportationLevers", config.useTeleportationLevers());
        useTeleportationMinigames = ShortestPathPlugin.override("useTeleportationMinigames", config.useTeleportationMinigames());
        useTeleportationPortals = ShortestPathPlugin.override("useTeleportationPortals", config.useTeleportationPortals());
        useTeleportationSpells = ShortestPathPlugin.override("useTeleportationSpells", config.useTeleportationSpells());
        useWildernessObelisks = ShortestPathPlugin.override("useWildernessObelisks", config.useWildernessObelisks());
        currencyThreshold = ShortestPathPlugin.override("currencyThreshold", config.currencyThreshold());

    }

    public boolean avoidWilderness(int packedPosition, int packedNeighborPosition, boolean targetInWilderness) {
        return avoidWilderness && !targetInWilderness
            && !WildernessChecker.isInWilderness(packedPosition) && WildernessChecker.isInWilderness(packedNeighborPosition);
    }

    private void refreshDestinations() {
        destinations = avoidWilderness ? filteredDestinations : allDestinations;
    }
}
