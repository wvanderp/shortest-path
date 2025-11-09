package shortestpath;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import shortestpath.pathfinder.Pathfinder;
import shortestpath.pathfinder.PathfinderConfig;
import shortestpath.transport.Transport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SnapshotTest {
    
    private PathfinderConfig pathfinderConfig;

    @Mock
    Client client;

    @Mock
    ShortestPathPlugin plugin;

    @Mock
    ShortestPathConfig config;

    @Before
    public void setUp() {
        // Configure basic pathfinder settings
        when(config.calculationCutoff()).thenReturn(30);
        when(config.currencyThreshold()).thenReturn(10000000);
    }

    @Test
    public void testRouteFromLumbridgeToVarrock() {
        // Setup character with basic skills (no special equipment needed for walking)
        setupConfig(QuestState.FINISHED, 99, TeleportationItem.NONE);
        
        // Given: Character at Lumbridge castle
        int startPoint = WorldPointUtil.packWorldPoint(3222, 3218, 0); // Lumbridge Castle
        int endPoint = WorldPointUtil.packWorldPoint(3213, 3424, 0);   // Varrock Square
        
        // When: Calculate the path
        Pathfinder pathfinder = new Pathfinder(plugin, pathfinderConfig, startPoint, Set.of(endPoint));
        pathfinder.run();
        PrimitiveIntList path = pathfinder.getPath();
        
        // Then: Verify path matches expected snapshot
        int[] expectedPath = loadSnapshot("lumbridge_to_varrock.txt");
        assertPathMatches(path, expectedPath, "Lumbridge to Varrock", startPoint, endPoint);
    }

    @Test
    public void testRouteFromDraynorToFalador() {
        // Setup character with basic skills (no special equipment needed for walking)
        setupConfig(QuestState.FINISHED, 99, TeleportationItem.NONE);
        
        // Given: Character at Draynor Village
        int startPoint = WorldPointUtil.packWorldPoint(3093, 3244, 0); // Draynor Village square
        int endPoint = WorldPointUtil.packWorldPoint(2965, 3379, 0);   // Falador center
        
        // When: Calculate the path
        Pathfinder pathfinder = new Pathfinder(plugin, pathfinderConfig, startPoint, Set.of(endPoint));
        pathfinder.run();
        PrimitiveIntList path = pathfinder.getPath();
        
        // Then: Verify path matches expected snapshot
        int[] expectedPath = loadSnapshot("draynor_to_falador.txt");
        assertPathMatches(path, expectedPath, "Draynor to Falador", startPoint, endPoint);
    }

    /**
     * Loads a path snapshot from a file in the snapshots directory.
     * Each line in the file should be in the format: x,y,plane
     */
    private int[] loadSnapshot(String filename) {
        List<Integer> points = new ArrayList<>();
        String resourcePath = "/shortestpath/snapshots/" + filename;
        InputStream is = getClass().getResourceAsStream(resourcePath);
        if (is == null) {
            // Resource not found - caller will handle (and may write a snapshot)
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length != 3) {
                    fail("Invalid snapshot format in " + filename + ": " + line);
                }

                int x = Integer.parseInt(parts[0].trim());
                int y = Integer.parseInt(parts[1].trim());
                int plane = Integer.parseInt(parts[2].trim());
                points.add(WorldPointUtil.packWorldPoint(x, y, plane));
            }
        } catch (IOException e) {
            fail("Failed to load snapshot file: " + filename + " - " + e.getMessage());
        }
        
        return points.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Asserts that the calculated path matches the expected snapshot
     */
    private void assertPathMatches(PrimitiveIntList actualPath, int[] expectedPath, 
                                   String routeName, int startPoint, int endPoint) {
        assertNotNull("Path should not be null", actualPath);

        // If expectedPath is null it means we couldn't find the snapshot resource.
        // In that case, write the actual path to a file in the system temp directory
        // to make it easy for the developer to capture the canonical snapshot.
        if (expectedPath == null) {
            String written = writeSnapshotToFile(routeName.replaceAll("\\s+","_").toLowerCase() + ".txt", actualPath);
            fail("Expected snapshot not found for " + routeName + ". Wrote generated snapshot to: " + written);
            return;
        }
        assertEquals("Path length should match expected for " + routeName, 
                    expectedPath.length, actualPath.size());
        
        // Assert each point in the path matches the expected snapshot
        for (int i = 0; i < expectedPath.length; i++) {
            if (expectedPath[i] != actualPath.get(i)) {
                int expectedX = WorldPointUtil.unpackWorldX(expectedPath[i]);
                int expectedY = WorldPointUtil.unpackWorldY(expectedPath[i]);
                int expectedPlane = WorldPointUtil.unpackWorldPlane(expectedPath[i]);
                int actualX = WorldPointUtil.unpackWorldX(actualPath.get(i));
                int actualY = WorldPointUtil.unpackWorldY(actualPath.get(i));
                int actualPlane = WorldPointUtil.unpackWorldPlane(actualPath.get(i));
                
                fail(String.format(
                    "%s: Point %d mismatch\n  Expected: (%d, %d, %d)\n  Actual:   (%d, %d, %d)",
                    routeName, i, expectedX, expectedY, expectedPlane, actualX, actualY, actualPlane
                ));
            }
        }
        
        // Also verify start and end for clarity
        assertEquals("Path should start at correct location", startPoint, actualPath.get(0));
        assertEquals("Path should end at correct location", endPoint, actualPath.get(actualPath.size() - 1));
        
        System.out.println("✓ " + routeName + " path verified: " + actualPath.size() + " points match expected snapshot");
    }

    /**
     * Writes the given path to a file under the system temporary directory and
     * returns the absolute path to the file written. The file format is CSV
     * with lines: x,y,plane
     */
    private String writeSnapshotToFile(String filename, PrimitiveIntList path) {
        // Prefer writing into the project's build directory so the file is easy to find and commit.
        String projectDir = System.getProperty("user.dir");
        Path buildDir = Paths.get(projectDir, "build", "test-snapshots");
        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"), "shortestpath", "snapshots");

        IOException lastEx = null;
        // Try build dir first
        try {
            Files.createDirectories(buildDir);
            Path file = buildDir.resolve(filename);
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                for (int i = 0; i < path.size(); i++) {
                    int packed = path.get(i);
                    int x = WorldPointUtil.unpackWorldX(packed);
                    int y = WorldPointUtil.unpackWorldY(packed);
                    int plane = WorldPointUtil.unpackWorldPlane(packed);
                    writer.write(String.format("%d,%d,%d", x, y, plane));
                    writer.newLine();
                }
            }
            return file.toAbsolutePath().toString();
        } catch (IOException e) {
            lastEx = e;
        }

        // Fallback to tmp dir
        try {
            Files.createDirectories(tmpDir);
            Path file = tmpDir.resolve(filename);
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                for (int i = 0; i < path.size(); i++) {
                    int packed = path.get(i);
                    int x = WorldPointUtil.unpackWorldX(packed);
                    int y = WorldPointUtil.unpackWorldY(packed);
                    int plane = WorldPointUtil.unpackWorldPlane(packed);
                    writer.write(String.format("%d,%d,%d", x, y, plane));
                    writer.newLine();
                }
            }
            return file.toAbsolutePath().toString();
        } catch (IOException e) {
            // If both fail, include both errors in the message
            String err = "<failed to write snapshot: ";
            if (lastEx != null) {
                err += lastEx.getMessage() + "; " + e.getMessage();
            } else {
                err += e.getMessage();
            }
            err += ">";
            return err;
        }
    }

    private void setupConfig(QuestState questState, int skillLevel, TeleportationItem useTeleportationItems) {
        pathfinderConfig = spy(new PathfinderConfig(client, config));

        when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
        when(client.getClientThread()).thenReturn(Thread.currentThread());
        when(client.getBoostedSkillLevel(any(Skill.class))).thenReturn(skillLevel);
        when(config.useTeleportationItems()).thenReturn(useTeleportationItems);
        doReturn(true).when(pathfinderConfig).varbitChecks(any(Transport.class));
        doReturn(true).when(pathfinderConfig).varPlayerChecks(any(Transport.class));
        doReturn(questState).when(pathfinderConfig).getQuestState(any(Quest.class));

        pathfinderConfig.refresh();
    }
}
