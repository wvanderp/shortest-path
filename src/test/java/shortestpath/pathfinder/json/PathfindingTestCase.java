package shortestpath.pathfinder.json;

import java.util.List;
import java.util.Map;

/**
 * Represents a complete pathfinding test case loaded from a JSON file.
 * Each JSON file contains exactly one test case with setup, test parameters, and expected results.
 */
public class PathfindingTestCase {
    /** Descriptive name for this test case */
    private String name;
    
    /** Optional description explaining what the test verifies */
    private String description;
    
    /** Setup configuration for the test */
    private TestSetup setup;
    
    /** The actual test parameters (start and end locations) */
    private TestPath test;
    
    /** Expected results of the pathfinding */
    private TestExpectation expected;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public TestSetup getSetup() {
        return setup;
    }
    
    public void setSetup(TestSetup setup) {
        this.setup = setup;
    }
    
    public TestPath getTest() {
        return test;
    }
    
    public void setTest(TestPath test) {
        this.test = test;
    }
    
    public TestExpectation getExpected() {
        return expected;
    }
    
    public void setExpected(TestExpectation expected) {
        this.expected = expected;
    }
    
    /**
     * Player setup configuration including inventory, equipment, and skills.
     */
    public static class TestSetup {
        /** Items in the player's inventory: map of item ID to quantity */
        private Map<Integer, Integer> inventory;
        
        /** Items equipped by the player: map of item ID to quantity */
        private Map<Integer, Integer> equipment;
        
        /** Player skill levels: map of skill name to level */
        private Map<String, Integer> skills;
        
        /** Quest states: map of quest name to state (NOT_STARTED, IN_PROGRESS, FINISHED) */
        private Map<String, String> quests;
        
        /** Varbit values: map of varbit ID to value */
        private Map<Integer, Integer> varbits;
        
        /** Pathfinder configuration settings */
        private PathfinderSettings pathfinder;
        
        public Map<Integer, Integer> getInventory() {
            return inventory;
        }
        
        public void setInventory(Map<Integer, Integer> inventory) {
            this.inventory = inventory;
        }
        
        public Map<Integer, Integer> getEquipment() {
            return equipment;
        }
        
        public void setEquipment(Map<Integer, Integer> equipment) {
            this.equipment = equipment;
        }
        
        public Map<String, Integer> getSkills() {
            return skills;
        }
        
        public void setSkills(Map<String, Integer> skills) {
            this.skills = skills;
        }
        
        public Map<String, String> getQuests() {
            return quests;
        }
        
        public void setQuests(Map<String, String> quests) {
            this.quests = quests;
        }
        
        public Map<Integer, Integer> getVarbits() {
            return varbits;
        }
        
        public void setVarbits(Map<Integer, Integer> varbits) {
            this.varbits = varbits;
        }
        
        public PathfinderSettings getPathfinder() {
            return pathfinder;
        }
        
        public void setPathfinder(PathfinderSettings pathfinder) {
            this.pathfinder = pathfinder;
        }
    }
    
    /**
     * Pathfinder-specific settings controlling which transports and features are enabled.
     */
    public static class PathfinderSettings {
        private Boolean avoidWilderness;
        private Boolean useAgilityShortcuts;
        private Boolean useGrappleShortcuts;
        private Boolean useBoats;
        private Boolean useCanoes;
        private Boolean useCharterShips;
        private Boolean useShips;
        private Boolean useFairyRings;
        private Boolean useGnomeGliders;
        private Boolean useHotAirBalloons;
        private Boolean useMagicCarpets;
        private Boolean useMagicMushtrees;
        private Boolean useMinecarts;
        private Boolean useQuetzals;
        private Boolean useSpiritTrees;
        private Boolean useTeleportationBoxes;
        private Boolean useTeleportationLevers;
        private Boolean useTeleportationPortals;
        private Boolean useTeleportationPortalsPoh;
        private Boolean useTeleportationSpells;
        private Boolean useTeleportationMinigames;
        private Boolean useWildernessObelisks;
        private Boolean useSeasonalTransports;
        /** One of: NONE, INVENTORY, INVENTORY_NON_CONSUMABLE, ALL, etc. */
        private String useTeleportationItems;
        private Integer currencyThreshold;
        private Integer calculationCutoff;
        
        public Boolean getAvoidWilderness() {
            return avoidWilderness;
        }
        
        public void setAvoidWilderness(Boolean avoidWilderness) {
            this.avoidWilderness = avoidWilderness;
        }
        
        public Boolean getUseAgilityShortcuts() {
            return useAgilityShortcuts;
        }
        
        public void setUseAgilityShortcuts(Boolean useAgilityShortcuts) {
            this.useAgilityShortcuts = useAgilityShortcuts;
        }
        
        public Boolean getUseGrappleShortcuts() {
            return useGrappleShortcuts;
        }
        
        public void setUseGrappleShortcuts(Boolean useGrappleShortcuts) {
            this.useGrappleShortcuts = useGrappleShortcuts;
        }
        
        public Boolean getUseBoats() {
            return useBoats;
        }
        
        public void setUseBoats(Boolean useBoats) {
            this.useBoats = useBoats;
        }
        
        public Boolean getUseCanoes() {
            return useCanoes;
        }
        
        public void setUseCanoes(Boolean useCanoes) {
            this.useCanoes = useCanoes;
        }
        
        public Boolean getUseCharterShips() {
            return useCharterShips;
        }
        
        public void setUseCharterShips(Boolean useCharterShips) {
            this.useCharterShips = useCharterShips;
        }
        
        public Boolean getUseShips() {
            return useShips;
        }
        
        public void setUseShips(Boolean useShips) {
            this.useShips = useShips;
        }
        
        public Boolean getUseFairyRings() {
            return useFairyRings;
        }
        
        public void setUseFairyRings(Boolean useFairyRings) {
            this.useFairyRings = useFairyRings;
        }
        
        public Boolean getUseGnomeGliders() {
            return useGnomeGliders;
        }
        
        public void setUseGnomeGliders(Boolean useGnomeGliders) {
            this.useGnomeGliders = useGnomeGliders;
        }
        
        public Boolean getUseHotAirBalloons() {
            return useHotAirBalloons;
        }
        
        public void setUseHotAirBalloons(Boolean useHotAirBalloons) {
            this.useHotAirBalloons = useHotAirBalloons;
        }
        
        public Boolean getUseMagicCarpets() {
            return useMagicCarpets;
        }
        
        public void setUseMagicCarpets(Boolean useMagicCarpets) {
            this.useMagicCarpets = useMagicCarpets;
        }
        
        public Boolean getUseMagicMushtrees() {
            return useMagicMushtrees;
        }
        
        public void setUseMagicMushtrees(Boolean useMagicMushtrees) {
            this.useMagicMushtrees = useMagicMushtrees;
        }
        
        public Boolean getUseMinecarts() {
            return useMinecarts;
        }
        
        public void setUseMinecarts(Boolean useMinecarts) {
            this.useMinecarts = useMinecarts;
        }
        
        public Boolean getUseQuetzals() {
            return useQuetzals;
        }
        
        public void setUseQuetzals(Boolean useQuetzals) {
            this.useQuetzals = useQuetzals;
        }
        
        public Boolean getUseSpiritTrees() {
            return useSpiritTrees;
        }
        
        public void setUseSpiritTrees(Boolean useSpiritTrees) {
            this.useSpiritTrees = useSpiritTrees;
        }
        
        public Boolean getUseTeleportationBoxes() {
            return useTeleportationBoxes;
        }
        
        public void setUseTeleportationBoxes(Boolean useTeleportationBoxes) {
            this.useTeleportationBoxes = useTeleportationBoxes;
        }
        
        public Boolean getUseTeleportationLevers() {
            return useTeleportationLevers;
        }
        
        public void setUseTeleportationLevers(Boolean useTeleportationLevers) {
            this.useTeleportationLevers = useTeleportationLevers;
        }
        
        public Boolean getUseTeleportationPortals() {
            return useTeleportationPortals;
        }
        
        public void setUseTeleportationPortals(Boolean useTeleportationPortals) {
            this.useTeleportationPortals = useTeleportationPortals;
        }
        
        public Boolean getUseTeleportationPortalsPoh() {
            return useTeleportationPortalsPoh;
        }
        
        public void setUseTeleportationPortalsPoh(Boolean useTeleportationPortalsPoh) {
            this.useTeleportationPortalsPoh = useTeleportationPortalsPoh;
        }
        
        public Boolean getUseTeleportationSpells() {
            return useTeleportationSpells;
        }
        
        public void setUseTeleportationSpells(Boolean useTeleportationSpells) {
            this.useTeleportationSpells = useTeleportationSpells;
        }
        
        public Boolean getUseTeleportationMinigames() {
            return useTeleportationMinigames;
        }
        
        public void setUseTeleportationMinigames(Boolean useTeleportationMinigames) {
            this.useTeleportationMinigames = useTeleportationMinigames;
        }
        
        public Boolean getUseWildernessObelisks() {
            return useWildernessObelisks;
        }
        
        public void setUseWildernessObelisks(Boolean useWildernessObelisks) {
            this.useWildernessObelisks = useWildernessObelisks;
        }
        
        public Boolean getUseSeasonalTransports() {
            return useSeasonalTransports;
        }
        
        public void setUseSeasonalTransports(Boolean useSeasonalTransports) {
            this.useSeasonalTransports = useSeasonalTransports;
        }
        
        public String getUseTeleportationItems() {
            return useTeleportationItems;
        }
        
        public void setUseTeleportationItems(String useTeleportationItems) {
            this.useTeleportationItems = useTeleportationItems;
        }
        
        public Integer getCurrencyThreshold() {
            return currencyThreshold;
        }
        
        public void setCurrencyThreshold(Integer currencyThreshold) {
            this.currencyThreshold = currencyThreshold;
        }
        
        public Integer getCalculationCutoff() {
            return calculationCutoff;
        }
        
        public void setCalculationCutoff(Integer calculationCutoff) {
            this.calculationCutoff = calculationCutoff;
        }
    }
    
    /**
     * Test parameters: the start and end locations for the path.
     */
    public static class TestPath {
        private WorldLocation start;
        private WorldLocation end;
        
        public WorldLocation getStart() {
            return start;
        }
        
        public void setStart(WorldLocation start) {
            this.start = start;
        }
        
        public WorldLocation getEnd() {
            return end;
        }
        
        public void setEnd(WorldLocation end) {
            this.end = end;
        }
    }
    
    /**
     * A world location with x, y, and plane coordinates.
     */
    public static class WorldLocation {
        private int x;
        private int y;
        private int plane;
        
        public WorldLocation() {}
        
        public WorldLocation(int x, int y, int plane) {
            this.x = x;
            this.y = y;
            this.plane = plane;
        }
        
        public int getX() {
            return x;
        }
        
        public void setX(int x) {
            this.x = x;
        }
        
        public int getY() {
            return y;
        }
        
        public void setY(int y) {
            this.y = y;
        }
        
        public int getPlane() {
            return plane;
        }
        
        public void setPlane(int plane) {
            this.plane = plane;
        }
        
        @Override
        public String toString() {
            return "(" + x + ", " + y + ", " + plane + ")";
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            WorldLocation that = (WorldLocation) obj;
            return x == that.x && y == that.y && plane == that.plane;
        }
        
        @Override
        public int hashCode() {
            return 31 * 31 * x + 31 * y + plane;
        }
    }
    
    /**
     * Expected results of the pathfinding test.
     */
    public static class TestExpectation {
        /** When true, update this test file with actual results from pathfinding (snapshot update mode) */
        private Boolean update;
        
        /** The expected path length (number of tiles) */
        private Integer pathLength;
        
        /** Minimum expected path length (for cases where exact length may vary) */
        private Integer minimumPathLength;
        
        /** Maximum expected path length */
        private Integer maximumPathLength;
        
        /** The exact expected path as a list of world locations (required) */
        private List<WorldLocation> path;
        
        /** Whether the path should be found at all */
        private Boolean pathFound;
        
        /** Expected transports used in the path (by type name) */
        private List<String> transportsUsed;
        
        public Boolean getUpdate() {
            return update;
        }
        
        public void setUpdate(Boolean update) {
            this.update = update;
        }
        
        public Integer getPathLength() {
            return pathLength;
        }
        
        public void setPathLength(Integer pathLength) {
            this.pathLength = pathLength;
        }
        
        public Integer getMinimumPathLength() {
            return minimumPathLength;
        }
        
        public void setMinimumPathLength(Integer minimumPathLength) {
            this.minimumPathLength = minimumPathLength;
        }
        
        public Integer getMaximumPathLength() {
            return maximumPathLength;
        }
        
        public void setMaximumPathLength(Integer maximumPathLength) {
            this.maximumPathLength = maximumPathLength;
        }
        
        public List<WorldLocation> getPath() {
            return path;
        }
        
        public void setPath(List<WorldLocation> path) {
            this.path = path;
        }
        
        public Boolean getPathFound() {
            return pathFound;
        }
        
        public void setPathFound(Boolean pathFound) {
            this.pathFound = pathFound;
        }
        
        public List<String> getTransportsUsed() {
            return transportsUsed;
        }
        
        public void setTransportsUsed(List<String> transportsUsed) {
            this.transportsUsed = transportsUsed;
        }
    }
}
