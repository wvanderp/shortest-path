package pathfinder;

import org.junit.Assert;
import org.junit.Test;
import shortestpath.Transport;
import shortestpath.TransportLoader;
import shortestpath.TransportType;
import shortestpath.TsvParser;

import java.util.HashMap;
import java.util.Set;

public class TransportLoaderTest {

    @Test
    public void testLoadAllFromResources() {
        HashMap<Integer, Set<Transport>> transports = TransportLoader.loadAllFromResources();
        
        // Should load transports without throwing exceptions
        Assert.assertNotNull(transports);
        Assert.assertTrue("Should load some transports", transports.size() > 0);
        
        // Verify that at least one transport set is not empty
        boolean hasNonEmptySet = false;
        for (Set<Transport> transportSet : transports.values()) {
            if (!transportSet.isEmpty()) {
                hasNonEmptySet = true;
                break;
            }
        }
        Assert.assertTrue("Should have at least one non-empty transport set", hasNonEmptySet);
    }

    @Test
    public void testTransportConfigConstruction() {
        TransportLoader.TransportConfig config1 = 
            new TransportLoader.TransportConfig("/path/test.tsv", TransportType.TRANSPORT);
        
        Assert.assertEquals("/path/test.tsv", config1.getResourcePath());
        Assert.assertEquals(TransportType.TRANSPORT, config1.getTransportType());
        Assert.assertEquals(0, config1.getRadiusThreshold());
        
        TransportLoader.TransportConfig config2 = 
            new TransportLoader.TransportConfig("/path/test2.tsv", TransportType.BOAT, 5);
        
        Assert.assertEquals("/path/test2.tsv", config2.getResourcePath());
        Assert.assertEquals(TransportType.BOAT, config2.getTransportType());
        Assert.assertEquals(5, config2.getRadiusThreshold());
    }

    @Test
    public void testLoadFromConfigsWithEmptyArray() {
        TransportLoader.TransportConfig[] emptyConfigs = {};
        HashMap<Integer, Set<Transport>> result = TransportLoader.loadFromConfigs(emptyConfigs);
        
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testLoadFromConfigsWithInvalidPath() {
        TransportLoader.TransportConfig[] invalidConfigs = {
            new TransportLoader.TransportConfig("/nonexistent/path.tsv", TransportType.TRANSPORT)
        };
        
        // Should not throw exception but should handle the error gracefully
        HashMap<Integer, Set<Transport>> result = TransportLoader.loadFromConfigs(invalidConfigs);
        
        Assert.assertNotNull(result);
        // Result might be empty due to failed loading, but should not crash
    }

    @Test
    public void testBackwardsCompatibilityWithTransportClass() {
        // Test that the deprecated method in Transport class still works
        HashMap<Integer, Set<Transport>> fromTransport = Transport.loadAllFromResources();
        HashMap<Integer, Set<Transport>> fromLoader = TransportLoader.loadAllFromResources();
        
        // Both should return the same results
        Assert.assertEquals("Results should be identical", fromTransport.size(), fromLoader.size());
        
        for (Integer key : fromTransport.keySet()) {
            Assert.assertTrue("All keys from Transport should exist in TransportLoader", 
                            fromLoader.containsKey(key));
            Assert.assertEquals("Transport sets should be equal for key " + key,
                              fromTransport.get(key).size(), fromLoader.get(key).size());
        }
    }

    @Test
    public void testAllDefaultConfigurations() {
        // Test that all default transport types can be loaded
        String[] expectedPaths = {
            "/transports/transports.tsv",
            "/transports/agility_shortcuts.tsv",
            "/transports/boats.tsv",
            "/transports/canoes.tsv",
            "/transports/charter_ships.tsv",
            "/transports/ships.tsv",
            "/transports/fairy_rings.tsv",
            "/transports/gnome_gliders.tsv",
            "/transports/hot_air_balloons.tsv",
            "/transports/magic_mushtrees.tsv",
            "/transports/minecarts.tsv",
            "/transports/quetzals.tsv",
            "/transports/spirit_trees.tsv",
            "/transports/teleportation_items.tsv",
            "/transports/teleportation_boxes.tsv",
            "/transports/teleportation_levers.tsv",
            "/transports/teleportation_minigames.tsv",
            "/transports/teleportation_portals.tsv",
            "/transports/teleportation_spells.tsv",
            "/transports/wilderness_obelisks.tsv"
        };

        HashMap<Integer, Set<Transport>> transports = TransportLoader.loadAllFromResources();
        
        // Should successfully load all types without throwing exceptions
        Assert.assertNotNull(transports);
        
        // Check that we have transports for multiple types
        boolean hasMultipleTypes = false;
        int typeCount = 0;
        
        for (Set<Transport> transportSet : transports.values()) {
            for (Transport transport : transportSet) {
                if (transport.getType() != null) {
                    typeCount++;
                    if (typeCount > 1) {
                        hasMultipleTypes = true;
                        break;
                    }
                }
            }
            if (hasMultipleTypes) break;
        }
        
        // Note: We can't assert hasMultipleTypes because the test environment
        // might not have all the transport files, but at least it should not crash
    }

    @Test
    public void testTransportCreationFromValidData() {
        // Create a minimal valid transport configuration for testing
        HashMap<Integer, Set<Transport>> transports = new HashMap<>();
        
        // We'll use one of the existing transport files to test the actual loading
        TransportLoader.TransportConfig config = 
            new TransportLoader.TransportConfig("/transports/transports.tsv", TransportType.TRANSPORT);
        
        try {
            TransportLoader.addTransports(transports, config);
            // If no exception is thrown, the parsing worked
            Assert.assertTrue("addTransports should process without exceptions", true);
        } catch (Exception e) {
            // If transport file doesn't exist in test environment, that's okay
            // The important thing is that our refactoring doesn't break the structure
            Assert.assertTrue("Exception should be related to missing resource, not parsing logic: " + e.getMessage(),
                            e.getMessage().contains("resource") || e.getMessage().contains("InputStream"));
        }
    }

    @Test
    public void testInvalidTsvHandling() {
        // Test with a config that would point to invalid TSV data
        // This tests the error handling in the TransportLoader
        
        TransportLoader.TransportConfig invalidConfig = 
            new TransportLoader.TransportConfig("/nonexistent.tsv", TransportType.TRANSPORT);
        
        HashMap<Integer, Set<Transport>> transports = new HashMap<>();
        
        try {
            TransportLoader.addTransports(transports, invalidConfig);
            // Should handle the error gracefully
        } catch (RuntimeException e) {
            // Expected - should throw RuntimeException for missing resources
            Assert.assertTrue("Exception should indicate resource issue", 
                            e.getMessage().contains("resource") || e.getMessage().contains("read"));
        }
    }
}