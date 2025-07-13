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
    public boolean avoidWilderness;
    @Getter
    public boolean useAgilityShortcuts;
    @Getter
    public boolean useGrappleShortcuts;
    @Getter
    public boolean useBoats;
    @Getter
    public boolean useCanoes;
    @Getter
    public boolean useCharterShips;
    @Getter
    public boolean useShips;
    @Getter
    public boolean useFairyRings;
    @Getter
    public boolean useGnomeGliders;
    @Getter
    public boolean useHotAirBalloons;
    @Getter
    public boolean useMinecarts;
    @Getter
    public boolean useQuetzals;
    @Getter
    public boolean useSpiritTrees;
    @Getter
    public boolean useTeleportationLevers;
    @Getter
    public boolean useTeleportationMinigames;
    @Getter
    public boolean useTeleportationPortals;
    @Getter
    public boolean useTeleportationSpells;
    @Getter
    public boolean useWildernessObelisks;
    @Getter
    public int currencyThreshold;
    @Getter
    public TeleportationItem useTeleportationItems;
    @Getter
    public long calculationCutoffMillis;

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
        useTeleportationMinigames = ShortestPathPlugin.override("useTeleportationMinigames",
                config.useTeleportationMinigames());
        useTeleportationPortals = ShortestPathPlugin.override("useTeleportationPortals",
                config.useTeleportationPortals());
        useTeleportationSpells = ShortestPathPlugin.override("useTeleportationSpells", config.useTeleportationSpells());
        useWildernessObelisks = ShortestPathPlugin.override("useWildernessObelisks", config.useWildernessObelisks());
        currencyThreshold = ShortestPathPlugin.override("currencyThreshold", config.currencyThreshold());

    }

    public boolean avoidWilderness(int packedPosition, int packedNeighborPosition, boolean targetInWilderness) {
        return avoidWilderness && !targetInWilderness
                && !WildernessChecker.isInWilderness(packedPosition)
                && WildernessChecker.isInWilderness(packedNeighborPosition);
    }

}
