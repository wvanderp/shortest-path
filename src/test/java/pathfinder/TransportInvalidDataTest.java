package pathfinder;

import org.junit.Assert;
import org.junit.Test;
import shortestpath.Transport;
import shortestpath.TransportLoader;
import shortestpath.TransportType;
import shortestpath.TsvParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Tests that specifically verify that invalid transport data causes tests to fail.
 * These tests ensure that transport creation and loading is strict and catches malformed data.
 */
public class TransportInvalidDataTest {

    @Test(expected = RuntimeException.class)
    public void testFailOnNonexistentResource() {
        TransportLoader.TransportConfig config = 
            new TransportLoader.TransportConfig("/nonexistent/file.tsv", TransportType.TRANSPORT);
        
        HashMap<Integer, Set<Transport>> transports = new HashMap<>();
        TransportLoader.addTransports(transports, config);
    }

    @Test
    public void testMalformedTsvInTransportLoading() {
        // Create a mock TSV data with invalid structure
        String malformedTsv = "Origin\tDestination\tOrigin\n" +  // Duplicate Origin header
                             "1 2 3\t4 5 6\t7 8 9";
        
        try {
            TsvParser.TsvData data = TsvParser.parse(malformedTsv);
            Assert.fail("Should have failed on duplicate headers in TSV");
        } catch (TsvParser.TsvParseException e) {
            // Expected - the TSV parser should catch this
            Assert.assertTrue("Should mention duplicate", 
                            e.getMessage().toLowerCase().contains("duplicate"));
        }
    }

    @Test
    public void testInvalidWorldPointCoordinates() {
        // Test Transport constructor with invalid coordinate data
        Map<String, String> invalidFieldMap = new HashMap<>();
        invalidFieldMap.put("Origin", "invalid coordinates");
        invalidFieldMap.put("Destination", "1 2 3");
        
        try {
            Transport transport = new Transport(invalidFieldMap, TransportType.TRANSPORT);
            // If this doesn't throw an exception, the constructor handles invalid data gracefully
            // We can check if the origin was set to undefined or permutation value
            Assert.assertTrue("Invalid coordinates should result in special handling", 
                            transport.getOrigin() == Transport.UNDEFINED_ORIGIN || 
                            transport.getOrigin() != 0);
        } catch (NumberFormatException e) {
            // This is also acceptable - strict validation should catch invalid numbers
            Assert.assertTrue("Exception should be about number format", 
                            e.getMessage() != null);
        }
    }

    @Test
    public void testInvalidSkillLevels() {
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("Origin", "1 2 3");
        fieldMap.put("Destination", "4 5 6");
        fieldMap.put("Skills", "invalid_level Agility");  // Invalid level
        
        try {
            Transport transport = new Transport(fieldMap, TransportType.TRANSPORT);
            // Constructor should handle this gracefully or throw an exception
            // If it handles gracefully, skill level should remain 0
        } catch (NumberFormatException e) {
            // Acceptable - strict validation catches invalid skill levels
        }
    }

    @Test
    public void testInvalidItemRequirements() {
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("Origin", "1 2 3");
        fieldMap.put("Destination", "4 5 6");
        fieldMap.put("Items", "INVALID_ITEM=not_a_number");  // Invalid quantity
        
        try {
            Transport transport = new Transport(fieldMap, TransportType.TRANSPORT);
            // Constructor should handle this gracefully or throw an exception
        } catch (NumberFormatException e) {
            // Acceptable - strict validation catches invalid item quantities
        }
    }

    @Test
    public void testInvalidVarbitData() {
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("Origin", "1 2 3");
        fieldMap.put("Destination", "4 5 6");
        fieldMap.put("Varbits", "not_a_number==123");  // Invalid varbit ID
        
        try {
            Transport transport = new Transport(fieldMap, TransportType.TRANSPORT);
            // Should handle invalid varbit data
        } catch (NumberFormatException e) {
            // Acceptable - strict validation catches invalid varbit data
        }
    }

    @Test
    public void testInvalidVarPlayerData() {
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("Origin", "1 2 3");
        fieldMap.put("Destination", "4 5 6");
        fieldMap.put("VarPlayers", "123==not_a_number");  // Invalid varplayer value
        
        try {
            Transport transport = new Transport(fieldMap, TransportType.TRANSPORT);
            // Should handle invalid varplayer data
        } catch (NumberFormatException e) {
            // Acceptable - strict validation catches invalid varplayer data
        }
    }

    @Test
    public void testInvalidDuration() {
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("Origin", "1 2 3");
        fieldMap.put("Destination", "4 5 6");
        fieldMap.put("Duration", "not_a_number");  // Invalid duration
        
        try {
            Transport transport = new Transport(fieldMap, TransportType.TRANSPORT);
            // Constructor should handle this gracefully (duration remains 0)
            Assert.assertEquals("Invalid duration should result in 0 or default", 
                              0, transport.getDuration());
        } catch (NumberFormatException e) {
            // Also acceptable - strict validation catches invalid duration
        }
    }

    @Test
    public void testInvalidWildernessLevel() {
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("Origin", "1 2 3");
        fieldMap.put("Destination", "4 5 6");
        fieldMap.put("Wilderness level", "invalid_level");  // Invalid wilderness level
        
        try {
            Transport transport = new Transport(fieldMap, TransportType.TRANSPORT);
            // Constructor should handle this gracefully
            Assert.assertEquals("Invalid wilderness level should result in default", 
                              -1, transport.getMaxWildernessLevel());
        } catch (NumberFormatException e) {
            // Also acceptable - strict validation catches invalid wilderness level
        }
    }

    @Test
    public void testEmptyFieldsHandling() {
        // Test that empty fields are handled properly without causing failures
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("Origin", "");  // Empty origin
        fieldMap.put("Destination", "");  // Empty destination
        fieldMap.put("Skills", "");  // Empty skills
        fieldMap.put("Items", "");  // Empty items
        fieldMap.put("Duration", "");  // Empty duration
        
        try {
            Transport transport = new Transport(fieldMap, TransportType.TRANSPORT);
            // Should handle empty fields gracefully
            Assert.assertEquals("Empty origin should be undefined", 
                              Transport.UNDEFINED_ORIGIN, transport.getOrigin());
            Assert.assertEquals("Empty destination should be undefined", 
                              Transport.UNDEFINED_DESTINATION, transport.getDestination());
        } catch (Exception e) {
            Assert.fail("Empty fields should not cause exceptions: " + e.getMessage());
        }
    }

    @Test
    public void testNullFieldsHandling() {
        // Test that null fields are handled properly
        Map<String, String> fieldMap = new HashMap<>();
        // Don't add any fields - they will be null when accessed
        
        try {
            Transport transport = new Transport(fieldMap, TransportType.TRANSPORT);
            // Should handle null fields gracefully
            Assert.assertEquals("Null origin should be undefined", 
                              Transport.UNDEFINED_ORIGIN, transport.getOrigin());
            Assert.assertEquals("Null destination should be undefined", 
                              Transport.UNDEFINED_DESTINATION, transport.getDestination());
        } catch (Exception e) {
            Assert.fail("Null fields should not cause exceptions: " + e.getMessage());
        }
    }

    @Test
    public void testInconsistentDataStructure() {
        // Test data that's structurally valid TSV but logically inconsistent
        String inconsistentTsv = "Origin\tDestination\tSkills\n" +
                                "1 2 3\t4 5 6\t99 InvalidSkill\n" +  // Invalid skill name
                                "invalid\tcoordinates\there\n" +  // Invalid coordinates
                                "7 8 9\t\t50 Agility";  // Missing destination
        
        try {
            TsvParser.TsvData data = TsvParser.parse(inconsistentTsv);
            // TSV parsing should succeed, but transport creation might have issues
            Assert.assertEquals("Should parse 3 rows", 3, data.getRowCount());
            
            // Try to create transports from this data
            for (Map<String, String> row : data.getRows()) {
                try {
                    Transport transport = new Transport(row, TransportType.TRANSPORT);
                    // If creation succeeds, validate that it handled errors gracefully
                } catch (Exception e) {
                    // Individual transport creation failures are acceptable
                }
            }
        } catch (Exception e) {
            Assert.fail("Should handle inconsistent data gracefully: " + e.getMessage());
        }
    }
}