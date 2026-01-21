package shortestpath.pathfinder.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.gameval.InventoryID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import shortestpath.PrimitiveIntList;
import shortestpath.ShortestPathConfig;
import shortestpath.ShortestPathPlugin;
import shortestpath.TeleportationItem;
import shortestpath.WorldPointUtil;
import shortestpath.pathfinder.Pathfinder;
import shortestpath.pathfinder.PathfinderConfig;
import shortestpath.pathfinder.json.PathfindingTestCase.PathfinderSettings;
import shortestpath.pathfinder.json.PathfindingTestCase.TestExpectation;
import shortestpath.pathfinder.json.PathfindingTestCase.TestSetup;
import shortestpath.pathfinder.json.PathfindingTestCase.WorldLocation;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Parameterized test runner that loads pathfinding tests from JSON files.
 * 
 * Each JSON file in src/test/resources/pathfinding-tests/ contains a single test case
 * that specifies:
 * - Setup: player configuration (inventory, equipment, skills) and pathfinder settings
 * - Test: start and end locations
 * - Expected: the expected path, length, or other assertions
 * 
 * The tests are run as JUnit parameterized tests, with each JSON file becoming a separate test.
 */
@RunWith(Parameterized.class)
public class PathfindingJsonTest {
    
    private static final String TEST_RESOURCE_PATH = "pathfinding-tests";
    
    /**
     * Custom TypeAdapter for WorldLocation that reads/writes as compact [x, y, plane] arrays.
     * Also supports reading the verbose {"x": ..., "y": ..., "plane": ...} format for backwards compatibility.
     */
    private static final TypeAdapter<WorldLocation> WORLD_LOCATION_ADAPTER = new TypeAdapter<WorldLocation>() {
        @Override
        public void write(JsonWriter out, WorldLocation loc) throws IOException {
            if (loc == null) {
                out.nullValue();
                return;
            }
            out.beginArray();
            out.value(loc.getX());
            out.value(loc.getY());
            out.value(loc.getPlane());
            out.endArray();
        }
        
        @Override
        public WorldLocation read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            
            if (in.peek() == JsonToken.BEGIN_ARRAY) {
                // Compact array format: [x, y, plane]
                in.beginArray();
                int x = in.nextInt();
                int y = in.nextInt();
                int plane = in.nextInt();
                in.endArray();
                return new WorldLocation(x, y, plane);
            } else {
                // Verbose object format: {"x": ..., "y": ..., "plane": ...}
                in.beginObject();
                int x = 0, y = 0, plane = 0;
                while (in.hasNext()) {
                    String name = in.nextName();
                    switch (name) {
                        case "x": x = in.nextInt(); break;
                        case "y": y = in.nextInt(); break;
                        case "plane": plane = in.nextInt(); break;
                        default: in.skipValue();
                    }
                }
                in.endObject();
                return new WorldLocation(x, y, plane);
            }
        }
    };
    
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(WorldLocation.class, WORLD_LOCATION_ADAPTER)
        .create();
    
    @Mock
    private Client client;
    
    @Mock
    private ItemContainer inventory;
    
    @Mock
    private ItemContainer equipment;
    
    @Mock
    private ShortestPathPlugin plugin;
    
    @Mock
    private ShortestPathConfig config;
    
    private PathfinderConfig pathfinderConfig;
    
    private final PathfindingTestCase testCase;
    private final String testFileName;
    private final Path testFilePath;
    
    public PathfindingJsonTest(PathfindingTestCase testCase, String testFileName, Path testFilePath) {
        this.testCase = testCase;
        this.testFileName = testFileName;
        this.testFilePath = testFilePath;
    }
    
    /**
     * Loads all JSON test files from the pathfinding-tests resource directory.
     */
    @Parameters(name = "{1}")
    public static Collection<Object[]> loadTestCases() throws IOException {
        List<Object[]> testCases = new ArrayList<>();
        
        // Try to load from classpath resources
        ClassLoader classLoader = PathfindingJsonTest.class.getClassLoader();
        URL resourceUrl = classLoader.getResource(TEST_RESOURCE_PATH);
        
        if (resourceUrl == null) {
            System.err.println("Warning: No pathfinding-tests directory found in resources");
            return testCases;
        }
        
        String protocol = resourceUrl.getProtocol();
        
        if ("file".equals(protocol)) {
            // Running from IDE or non-JAR environment
            try {
                Path resourceDir = Paths.get(resourceUrl.toURI());
                if (Files.isDirectory(resourceDir)) {
                    Files.list(resourceDir)
                        .filter(path -> path.toString().endsWith(".json"))
                        .forEach(path -> {
                            try {
                                String content = Files.readString(path, StandardCharsets.UTF_8);
                                PathfindingTestCase tc = GSON.fromJson(content, PathfindingTestCase.class);
                                String fileName = path.getFileName().toString();
                                String displayName = tc.getName() != null ? tc.getName() : fileName;
                                // Also find the source file path for updating
                                Path sourceFilePath = findSourceFilePath(path);
                                testCases.add(new Object[]{tc, displayName, sourceFilePath});
                            } catch (IOException e) {
                                System.err.println("Failed to load test file: " + path + " - " + e.getMessage());
                            }
                        });
                }
            } catch (URISyntaxException e) {
                throw new IOException("Failed to read resource directory", e);
            }
        } else if ("jar".equals(protocol)) {
            // Running from JAR - update not supported
            String jarPath = resourceUrl.getPath().substring(5, resourceUrl.getPath().indexOf("!"));
            try (JarFile jar = new JarFile(jarPath)) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (name.startsWith(TEST_RESOURCE_PATH + "/") && name.endsWith(".json")) {
                        try (InputStream is = classLoader.getResourceAsStream(name);
                             Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                            PathfindingTestCase tc = GSON.fromJson(reader, PathfindingTestCase.class);
                            String fileName = name.substring(name.lastIndexOf('/') + 1);
                            String displayName = tc.getName() != null ? tc.getName() : fileName;
                            testCases.add(new Object[]{tc, displayName, null});
                        }
                    }
                }
            }
        }
        
        return testCases;
    }
    
    /**
     * Finds the source file path (in src/test/resources) for a resource file.
     * This is needed to update the JSON file when update mode is enabled.
     */
    private static Path findSourceFilePath(Path resourcePath) {
        // The resourcePath is in build/resources/test/pathfinding-tests/
        // We need to find src/test/resources/pathfinding-tests/
        String pathStr = resourcePath.toString();
        
        // Try to find the source directory by looking for common build output patterns
        String[] buildPatterns = {
            "build/resources/test",
            "build\\resources\\test",
            "bin/test",
            "bin\\test",
            "target/test-classes",
            "target\\test-classes"
        };
        
        for (String pattern : buildPatterns) {
            int idx = pathStr.indexOf(pattern);
            if (idx != -1) {
                String projectRoot = pathStr.substring(0, idx);
                String relativePath = pathStr.substring(idx + pattern.length());
                Path sourcePath = Paths.get(projectRoot, "src", "test", "resources", relativePath);
                if (Files.exists(sourcePath)) {
                    return sourcePath;
                }
            }
        }
        
        // If running directly from source, the path might already be correct
        if (pathStr.contains("src/test/resources") || pathStr.contains("src\\test\\resources")) {
            return resourcePath;
        }
        
        return null;
    }
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up default config values
        when(config.calculationCutoff()).thenReturn(30);
        when(config.currencyThreshold()).thenReturn(10000000);
        when(config.avoidWilderness()).thenReturn(true);
        when(config.useAgilityShortcuts()).thenReturn(false);
        when(config.useGrappleShortcuts()).thenReturn(false);
        when(config.useBoats()).thenReturn(false);
        when(config.useCanoes()).thenReturn(false);
        when(config.useCharterShips()).thenReturn(false);
        when(config.useShips()).thenReturn(false);
        when(config.useFairyRings()).thenReturn(false);
        when(config.useGnomeGliders()).thenReturn(false);
        when(config.useHotAirBalloons()).thenReturn(false);
        when(config.useMagicCarpets()).thenReturn(false);
        when(config.useMagicMushtrees()).thenReturn(false);
        when(config.useMinecarts()).thenReturn(false);
        when(config.useQuetzals()).thenReturn(false);
        when(config.useSpiritTrees()).thenReturn(false);
        when(config.useTeleportationItems()).thenReturn(TeleportationItem.NONE);
        when(config.useTeleportationBoxes()).thenReturn(false);
        when(config.useTeleportationLevers()).thenReturn(false);
        when(config.useTeleportationPortals()).thenReturn(false);
        when(config.useTeleportationPortalsPoh()).thenReturn(false);
        when(config.useTeleportationSpells()).thenReturn(false);
        when(config.useTeleportationMinigames()).thenReturn(false);
        when(config.useWildernessObelisks()).thenReturn(false);
        when(config.useSeasonalTransports()).thenReturn(false);
    }
    
    @Test
    public void runJsonTest() {
        assertNotNull("Test case should not be null", testCase);
        assertNotNull("Test section should not be null", testCase.getTest());
        assertNotNull("Test start location should not be null", testCase.getTest().getStart());
        assertNotNull("Test end location should not be null", testCase.getTest().getEnd());
        assertNotNull("Expected section should not be null", testCase.getExpected());
        
        // Apply setup configuration
        applySetup(testCase.getSetup());
        
        // Run pathfinding
        int startPacked = packLocation(testCase.getTest().getStart());
        int endPacked = packLocation(testCase.getTest().getEnd());
        
        Pathfinder pathfinder = new Pathfinder(plugin, pathfinderConfig, startPacked, Set.of(endPacked));
        pathfinder.run();
        
        PrimitiveIntList path = pathfinder.getPath();
        
        // Check if we should update the test file with actual results
        TestExpectation expected = testCase.getExpected();
        if (Boolean.TRUE.equals(expected.getUpdate())) {
            updateTestFile(path);
            System.out.println("Updated JSON test file: " + testFileName);
            // After updating, the test passes (it's a snapshot update)
            return;
        }
        
        // Path is required - verify it exists
        assertNotNull("Expected path must be specified (use 'update: true' to generate it)", expected.getPath());
        
        // Verify expectations
        verifyExpectations(expected, path, startPacked, endPacked);
        
        System.out.println("Successfully completed JSON test: " + testFileName);
    }
    
    /**
     * Updates the test JSON file with actual results from pathfinding.
     * Sets update to false, updates pathLength, pathFound, and path array.
     */
    private void updateTestFile(PrimitiveIntList actualPath) {
        if (testFilePath == null) {
            System.err.println("Cannot update test file: source file path not available (running from JAR?)");
            return;
        }
        
        TestExpectation expected = testCase.getExpected();
        
        // Update expected values with actual results
        expected.setUpdate(false);
        expected.setPathFound(actualPath.size() > 0);
        expected.setPathLength(actualPath.size());
        
        // Convert actual path to WorldLocation list
        List<WorldLocation> pathLocations = new ArrayList<>();
        for (int i = 0; i < actualPath.size(); i++) {
            int packed = actualPath.get(i);
            pathLocations.add(new WorldLocation(
                WorldPointUtil.unpackWorldX(packed),
                WorldPointUtil.unpackWorldY(packed),
                WorldPointUtil.unpackWorldPlane(packed)
            ));
        }
        expected.setPath(pathLocations);
        
        // Clear min/max constraints since we now have exact values
        expected.setMinimumPathLength(null);
        expected.setMaximumPathLength(null);
        
        // Write updated JSON back to source file
        try (Writer writer = new FileWriter(testFilePath.toFile(), StandardCharsets.UTF_8)) {
            GSON.toJson(testCase, writer);
            System.out.println("Updated test file: " + testFilePath);
        } catch (IOException e) {
            System.err.println("Failed to update test file: " + testFilePath + " - " + e.getMessage());
            throw new RuntimeException("Failed to update test file", e);
        }
    }
    
    private void applySetup(TestSetup setup) {
        int defaultSkillLevel = 99;
        QuestState defaultQuestState = QuestState.FINISHED;
        
        if (setup != null) {
            // Apply skill levels
            if (setup.getSkills() != null) {
                for (Map.Entry<String, Integer> entry : setup.getSkills().entrySet()) {
                    Skill skill = Skill.valueOf(entry.getKey().toUpperCase());
                    when(client.getBoostedSkillLevel(skill)).thenReturn(entry.getValue());
                }
                // Set a default for skills not specified
                when(client.getBoostedSkillLevel(any(Skill.class))).thenAnswer(invocation -> {
                    Skill skill = invocation.getArgument(0);
                    if (setup.getSkills().containsKey(skill.name())) {
                        return setup.getSkills().get(skill.name());
                    }
                    return defaultSkillLevel;
                });
            } else {
                when(client.getBoostedSkillLevel(any(Skill.class))).thenReturn(defaultSkillLevel);
            }
            
            // Apply inventory
            if (setup.getInventory() != null && !setup.getInventory().isEmpty()) {
                Item[] items = setup.getInventory().entrySet().stream()
                    .map(e -> new Item(e.getKey(), e.getValue()))
                    .toArray(Item[]::new);
                doReturn(inventory).when(client).getItemContainer(InventoryID.INV);
                doReturn(items).when(inventory).getItems();
            } else {
                doReturn(inventory).when(client).getItemContainer(InventoryID.INV);
                doReturn(new Item[0]).when(inventory).getItems();
            }
            
            // Apply equipment
            if (setup.getEquipment() != null && !setup.getEquipment().isEmpty()) {
                Item[] items = setup.getEquipment().entrySet().stream()
                    .map(e -> new Item(e.getKey(), e.getValue()))
                    .toArray(Item[]::new);
                doReturn(equipment).when(client).getItemContainer(InventoryID.WORN);
                doReturn(items).when(equipment).getItems();
            } else {
                doReturn(equipment).when(client).getItemContainer(InventoryID.WORN);
                doReturn(new Item[0]).when(equipment).getItems();
            }
            
            // Apply varbits
            if (setup.getVarbits() != null) {
                for (Map.Entry<Integer, Integer> entry : setup.getVarbits().entrySet()) {
                    when(client.getVarbitValue(entry.getKey())).thenReturn(entry.getValue());
                }
            }
            
            // Apply pathfinder settings
            if (setup.getPathfinder() != null) {
                applyPathfinderSettings(setup.getPathfinder());
            }
        } else {
            when(client.getBoostedSkillLevel(any(Skill.class))).thenReturn(defaultSkillLevel);
            doReturn(inventory).when(client).getItemContainer(InventoryID.INV);
            doReturn(new Item[0]).when(inventory).getItems();
            doReturn(equipment).when(client).getItemContainer(InventoryID.WORN);
            doReturn(new Item[0]).when(equipment).getItems();
        }
        
        // Set up common client mocks
        when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
        when(client.getClientThread()).thenReturn(Thread.currentThread());
        
        // Create and configure PathfinderConfig
        pathfinderConfig = spy(new PathfinderConfig(client, config));
        doReturn(defaultQuestState).when(pathfinderConfig).getQuestState(any(Quest.class));
        doReturn(true).when(pathfinderConfig).varbitChecks(any());
        doReturn(true).when(pathfinderConfig).varPlayerChecks(any());
        
        pathfinderConfig.refresh();
    }
    
    private void applyPathfinderSettings(PathfinderSettings settings) {
        if (settings.getAvoidWilderness() != null) {
            when(config.avoidWilderness()).thenReturn(settings.getAvoidWilderness());
        }
        if (settings.getUseAgilityShortcuts() != null) {
            when(config.useAgilityShortcuts()).thenReturn(settings.getUseAgilityShortcuts());
        }
        if (settings.getUseGrappleShortcuts() != null) {
            when(config.useGrappleShortcuts()).thenReturn(settings.getUseGrappleShortcuts());
        }
        if (settings.getUseBoats() != null) {
            when(config.useBoats()).thenReturn(settings.getUseBoats());
        }
        if (settings.getUseCanoes() != null) {
            when(config.useCanoes()).thenReturn(settings.getUseCanoes());
        }
        if (settings.getUseCharterShips() != null) {
            when(config.useCharterShips()).thenReturn(settings.getUseCharterShips());
        }
        if (settings.getUseShips() != null) {
            when(config.useShips()).thenReturn(settings.getUseShips());
        }
        if (settings.getUseFairyRings() != null) {
            when(config.useFairyRings()).thenReturn(settings.getUseFairyRings());
        }
        if (settings.getUseGnomeGliders() != null) {
            when(config.useGnomeGliders()).thenReturn(settings.getUseGnomeGliders());
        }
        if (settings.getUseHotAirBalloons() != null) {
            when(config.useHotAirBalloons()).thenReturn(settings.getUseHotAirBalloons());
        }
        if (settings.getUseMagicCarpets() != null) {
            when(config.useMagicCarpets()).thenReturn(settings.getUseMagicCarpets());
        }
        if (settings.getUseMagicMushtrees() != null) {
            when(config.useMagicMushtrees()).thenReturn(settings.getUseMagicMushtrees());
        }
        if (settings.getUseMinecarts() != null) {
            when(config.useMinecarts()).thenReturn(settings.getUseMinecarts());
        }
        if (settings.getUseQuetzals() != null) {
            when(config.useQuetzals()).thenReturn(settings.getUseQuetzals());
        }
        if (settings.getUseSpiritTrees() != null) {
            when(config.useSpiritTrees()).thenReturn(settings.getUseSpiritTrees());
        }
        if (settings.getUseTeleportationBoxes() != null) {
            when(config.useTeleportationBoxes()).thenReturn(settings.getUseTeleportationBoxes());
        }
        if (settings.getUseTeleportationLevers() != null) {
            when(config.useTeleportationLevers()).thenReturn(settings.getUseTeleportationLevers());
        }
        if (settings.getUseTeleportationPortals() != null) {
            when(config.useTeleportationPortals()).thenReturn(settings.getUseTeleportationPortals());
        }
        if (settings.getUseTeleportationPortalsPoh() != null) {
            when(config.useTeleportationPortalsPoh()).thenReturn(settings.getUseTeleportationPortalsPoh());
        }
        if (settings.getUseTeleportationSpells() != null) {
            when(config.useTeleportationSpells()).thenReturn(settings.getUseTeleportationSpells());
        }
        if (settings.getUseTeleportationMinigames() != null) {
            when(config.useTeleportationMinigames()).thenReturn(settings.getUseTeleportationMinigames());
        }
        if (settings.getUseWildernessObelisks() != null) {
            when(config.useWildernessObelisks()).thenReturn(settings.getUseWildernessObelisks());
        }
        if (settings.getUseSeasonalTransports() != null) {
            when(config.useSeasonalTransports()).thenReturn(settings.getUseSeasonalTransports());
        }
        if (settings.getUseTeleportationItems() != null) {
            TeleportationItem item = TeleportationItem.valueOf(settings.getUseTeleportationItems());
            when(config.useTeleportationItems()).thenReturn(item);
        }
        if (settings.getCurrencyThreshold() != null) {
            when(config.currencyThreshold()).thenReturn(settings.getCurrencyThreshold());
        }
        if (settings.getCalculationCutoff() != null) {
            when(config.calculationCutoff()).thenReturn(settings.getCalculationCutoff());
        }
    }
    
    private void verifyExpectations(TestExpectation expected, PrimitiveIntList path, int startPacked, int endPacked) {
        int pathLength = path.size();
        
        // Check if path was found
        if (expected.getPathFound() != null) {
            if (expected.getPathFound()) {
                assertTrue("Expected path to be found but it was empty", pathLength > 0);
            } else {
                assertEquals("Expected no path to be found", 0, pathLength);
                return; // No further checks if path not expected
            }
        }
        
        // Check exact path length
        if (expected.getPathLength() != null) {
            assertEquals(
                buildPathLengthErrorMessage("Path length mismatch", startPacked, endPacked, pathLength),
                (int) expected.getPathLength(), 
                pathLength
            );
        }
        
        // Check minimum path length
        if (expected.getMinimumPathLength() != null) {
            assertTrue(
                buildPathLengthErrorMessage(
                    "Path length " + pathLength + " is less than minimum " + expected.getMinimumPathLength(),
                    startPacked, endPacked, pathLength
                ),
                pathLength >= expected.getMinimumPathLength()
            );
        }
        
        // Check maximum path length
        if (expected.getMaximumPathLength() != null) {
            assertTrue(
                buildPathLengthErrorMessage(
                    "Path length " + pathLength + " is greater than maximum " + expected.getMaximumPathLength(),
                    startPacked, endPacked, pathLength
                ),
                pathLength <= expected.getMaximumPathLength()
            );
        }
        
        // Check exact path if specified
        if (expected.getPath() != null && !expected.getPath().isEmpty()) {
            assertEquals("Path size does not match expected path", expected.getPath().size(), pathLength);
            
            for (int i = 0; i < pathLength; i++) {
                int actualPacked = path.get(i);
                WorldLocation expectedLoc = expected.getPath().get(i);
                int expectedPacked = packLocation(expectedLoc);
                
                assertEquals(
                    "Path mismatch at index " + i + ": expected " + expectedLoc + 
                    " but got (" + WorldPointUtil.unpackWorldX(actualPacked) + 
                    ", " + WorldPointUtil.unpackWorldY(actualPacked) + 
                    ", " + WorldPointUtil.unpackWorldPlane(actualPacked) + ")",
                    expectedPacked, 
                    actualPacked
                );
            }
        }
    }
    
    private String buildPathLengthErrorMessage(String message, int startPacked, int endPacked, int actualLength) {
        return message + " from (" + 
            WorldPointUtil.unpackWorldX(startPacked) + ", " + 
            WorldPointUtil.unpackWorldY(startPacked) + ", " + 
            WorldPointUtil.unpackWorldPlane(startPacked) + ") to (" + 
            WorldPointUtil.unpackWorldX(endPacked) + ", " + 
            WorldPointUtil.unpackWorldY(endPacked) + ", " + 
            WorldPointUtil.unpackWorldPlane(endPacked) + "). Actual path length: " + actualLength;
    }
    
    private int packLocation(WorldLocation loc) {
        return WorldPointUtil.packWorldPoint(loc.getX(), loc.getY(), loc.getPlane());
    }
}
