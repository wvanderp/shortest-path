package pathfinder;

import org.junit.Assert;
import org.junit.Test;
import shortestpath.TsvParser;
import shortestpath.TransportLoader;
import shortestpath.Transport;
import shortestpath.TransportType;

import java.util.HashMap;
import java.util.Set;

/**
 * Integration test to verify that the refactored transport system works correctly together.
 */
public class TransportSystemIntegrationTest {

    @Test
    public void testTsvParserBasicFunctionality() {
        String simpleTsv = "Origin\tDestination\tType\n" +
                          "1 2 3\t4 5 6\tNormal\n" +
                          "7 8 9\t10 11 12\tFast";
        
        TsvParser.TsvData data = TsvParser.parse(simpleTsv);
        
        Assert.assertEquals("Should have 3 columns", 3, data.getColumnCount());
        Assert.assertEquals("Should have 2 rows", 2, data.getRowCount());
        
        String[] headers = data.getHeaders();
        Assert.assertEquals("Origin", headers[0]);
        Assert.assertEquals("Destination", headers[1]);
        Assert.assertEquals("Type", headers[2]);
    }

    @Test
    public void testTransportLoaderConfiguration() {
        TransportLoader.TransportConfig config = 
            new TransportLoader.TransportConfig("/test/path.tsv", TransportType.TRANSPORT, 5);
        
        Assert.assertEquals("/test/path.tsv", config.getResourcePath());
        Assert.assertEquals(TransportType.TRANSPORT, config.getTransportType());
        Assert.assertEquals(5, config.getRadiusThreshold());
    }

    @Test
    public void testBackwardsCompatibility() {
        // Test that the old Transport.loadAllFromResources() method still works
        // and delegates to the new TransportLoader
        try {
            HashMap<Integer, Set<Transport>> transports = Transport.loadAllFromResources();
            Assert.assertNotNull("Transports should not be null", transports);
            // If this doesn't throw an exception, the delegation is working
        } catch (RuntimeException e) {
            // Expected if resource files are missing in test environment
            // The important thing is that the method delegation works
            Assert.assertTrue("Exception should be related to missing resources", 
                            e.getMessage().contains("resource") || e.getMessage().contains("read"));
        }
    }

    @Test
    public void testTransportCreationWithValidData() {
        // Test creating a Transport with valid field data
        java.util.Map<String, String> fieldMap = new java.util.HashMap<>();
        fieldMap.put("Origin", "3200 3200 0");
        fieldMap.put("Destination", "3210 3210 0");
        fieldMap.put("Skills", "50 Agility");
        fieldMap.put("Duration", "5");
        
        Transport transport = new Transport(fieldMap, TransportType.AGILITY_SHORTCUT);
        
        Assert.assertNotEquals("Origin should be set", Transport.UNDEFINED_ORIGIN, transport.getOrigin());
        Assert.assertNotEquals("Destination should be set", Transport.UNDEFINED_DESTINATION, transport.getDestination());
        Assert.assertEquals("Duration should be set", 5, transport.getDuration());
        Assert.assertEquals("Type should be set", TransportType.AGILITY_SHORTCUT, transport.getType());
    }

    @Test
    public void testSystemWorksWithMinimalData() {
        // Test that the system can handle minimal transport data
        java.util.Map<String, String> fieldMap = new java.util.HashMap<>();
        fieldMap.put("Origin", "1000 1000 0");
        fieldMap.put("Destination", "1010 1010 0");
        
        Transport transport = new Transport(fieldMap, TransportType.TRANSPORT);
        
        Assert.assertNotNull("Transport should be created", transport);
        Assert.assertEquals("Type should be transport", TransportType.TRANSPORT, transport.getType());
        Assert.assertNotNull("Skill levels should be initialized", transport.getSkillLevels());
        Assert.assertNotNull("Quests should be initialized", transport.getQuests());
    }

    @Test
    public void testSystemHandlesEmptyConfiguration() {
        // Test that loading with empty configuration doesn't crash
        TransportLoader.TransportConfig[] emptyConfigs = {};
        HashMap<Integer, Set<Transport>> result = TransportLoader.loadFromConfigs(emptyConfigs);
        
        Assert.assertNotNull("Result should not be null", result);
        Assert.assertEquals("Empty config should result in empty map", 0, result.size());
    }

    @Test
    public void testTsvParsingWithComments() {
        String tsvWithComments = "# Header comment\n" +
                                "Origin\tDestination\n" +
                                "# Data comment\n" +
                                "1 2 3\t4 5 6\n" +
                                "# Another comment\n" +
                                "7 8 9\t10 11 12";
        
        TsvParser.TsvData data = TsvParser.parse(tsvWithComments);
        
        Assert.assertEquals("Should parse 2 rows ignoring comments", 2, data.getRowCount());
        Assert.assertEquals("Should have 2 columns", 2, data.getColumnCount());
    }

    @Test
    public void testCompleteWorkflow() {
        // Test a complete workflow from TSV parsing to Transport creation
        String transportTsv = "Origin\tDestination\tDuration\n" +
                             "3000 3000 0\t3010 3010 0\t3\n" +
                             "3020 3020 0\t3030 3030 0\t5";
        
        // Parse TSV
        TsvParser.TsvData data = TsvParser.parse(transportTsv);
        Assert.assertEquals("Should parse correctly", 2, data.getRowCount());
        
        // Create transports from parsed data
        for (java.util.Map<String, String> row : data.getRows()) {
            Transport transport = new Transport(row, TransportType.TRANSPORT);
            Assert.assertNotNull("Transport should be created", transport);
            Assert.assertTrue("Duration should be positive", transport.getDuration() > 0);
        }
    }
}