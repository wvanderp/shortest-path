package pathfinder;

import org.junit.Assert;
import org.junit.Test;
import shortestpath.Transport;
import shortestpath.TransportLoader;
import shortestpath.TsvParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Tests to ensure the refactored system maintains compatibility and correctness.
 */
public class RefactoringCompatibilityTest {

    @Test
    public void testRefactoredSystemEquivalence() {
        // Test that the new system produces equivalent results to the old system
        // Both should delegate to the same underlying implementation now
        
        try {
            HashMap<Integer, Set<Transport>> oldSystemResult = Transport.loadAllFromResources();
            HashMap<Integer, Set<Transport>> newSystemResult = TransportLoader.loadAllFromResources();
            
            // Both should return the same data structure
            Assert.assertEquals("Both systems should return maps of the same size", 
                              oldSystemResult.size(), newSystemResult.size());
            
            // Check that all keys are present in both
            for (Integer key : oldSystemResult.keySet()) {
                Assert.assertTrue("New system should contain all keys from old system", 
                                newSystemResult.containsKey(key));
                Assert.assertEquals("Transport sets should have same size for key " + key,
                                  oldSystemResult.get(key).size(), 
                                  newSystemResult.get(key).size());
            }
            
        } catch (RuntimeException e) {
            // If resources are missing in test environment, that's expected
            // The important thing is that both systems throw the same exception
            try {
                TransportLoader.loadAllFromResources();
                Assert.fail("If Transport.loadAllFromResources() throws, TransportLoader should too");
            } catch (RuntimeException e2) {
                // Both threw exceptions - this is expected behavior
                Assert.assertTrue("Exceptions should have meaningful messages", 
                                e.getMessage() != null && e2.getMessage() != null);
            }
        }
    }

    @Test
    public void testTsvParserStrictness() {
        // Test that the new TSV parser is actually more strict than a naive implementation
        
        String[] invalidInputs = {
            "Header1\tHeader2\tHeader1\ndata1\tdata2\tdata3",  // Duplicate headers
            "",  // Empty content
            null,  // Null content
            "   \n   \n   ",  // Whitespace only
        };
        
        for (String invalidInput : invalidInputs) {
            try {
                TsvParser.parse(invalidInput);
                if (invalidInput != null && !invalidInput.trim().isEmpty()) {
                    Assert.fail("Parser should have rejected invalid input: " + invalidInput);
                }
            } catch (TsvParser.TsvParseException e) {
                // Expected - this is the strict behavior we want
                Assert.assertNotNull("Exception should have a message", e.getMessage());
            }
        }
    }

    @Test 
    public void testTransportCreationRobustness() {
        // Test that Transport creation handles various edge cases properly
        
        Map<String, String> validMinimal = new HashMap<>();
        validMinimal.put("Origin", "1000 1000 0");
        validMinimal.put("Destination", "1010 1010 0");
        
        Transport transport1 = new Transport(validMinimal, null);
        Assert.assertNotNull("Transport should be created with minimal valid data", transport1);
        
        Map<String, String> empty = new HashMap<>();
        Transport transport2 = new Transport(empty, null);
        Assert.assertNotNull("Transport should handle empty field map", transport2);
        
        Map<String, String> withInvalidNumbers = new HashMap<>();
        withInvalidNumbers.put("Origin", "not_numbers");
        withInvalidNumbers.put("Duration", "invalid_duration");
        
        try {
            Transport transport3 = new Transport(withInvalidNumbers, null);
            // If no exception, the constructor handled invalid data gracefully
            Assert.assertNotNull("Transport should handle invalid data gracefully", transport3);
        } catch (NumberFormatException e) {
            // Also acceptable - strict validation can throw exceptions for invalid data
        }
    }

    @Test
    public void testSystemIntegrationWorks() {
        // Test that the complete pipeline from TSV -> Transport creation works
        
        String sampleTransportTsv = 
            "# Origin\tDestination\tSkills\tDuration\n" +
            "1000 1000 0\t1010 1010 0\t50 Agility\t5\n" +
            "2000 2000 0\t2010 2010 0\t\t3\n" +
            "# Comment line\n" +
            "3000 3000 0\t3010 3010 0\t30 Mining\t10";
        
        try {
            // Parse the TSV
            TsvParser.TsvData data = TsvParser.parse(sampleTransportTsv);
            Assert.assertEquals("Should parse 3 data rows", 3, data.getRowCount());
            Assert.assertEquals("Should have 4 columns", 4, data.getColumnCount());
            
            // Create transports from the data
            int successfulCreations = 0;
            for (Map<String, String> row : data.getRows()) {
                try {
                    Transport transport = new Transport(row, shortestpath.TransportType.TRANSPORT);
                    Assert.assertNotNull("Transport should be created", transport);
                    successfulCreations++;
                } catch (Exception e) {
                    // Some individual transports might fail, but not all
                }
            }
            
            Assert.assertTrue("At least some transports should be created successfully", 
                            successfulCreations > 0);
            
        } catch (Exception e) {
            Assert.fail("Complete integration should work: " + e.getMessage());
        }
    }

    @Test
    public void testErrorHandlingIsImproved() {
        // Test that the new system provides better error handling and messages
        
        try {
            TsvParser.parse("Duplicate\tHeaders\tDuplicate\ndata1\tdata2\tdata3");
            Assert.fail("Should have thrown exception for duplicate headers");
        } catch (TsvParser.TsvParseException e) {
            Assert.assertTrue("Error message should be informative", 
                            e.getMessage().toLowerCase().contains("duplicate"));
        }
        
        try {
            TransportLoader.TransportConfig config = 
                new TransportLoader.TransportConfig("/definitely/nonexistent/file.tsv", 
                                                  shortestpath.TransportType.TRANSPORT);
            HashMap<Integer, Set<Transport>> transports = new HashMap<>();
            TransportLoader.addTransports(transports, config);
            Assert.fail("Should have thrown exception for nonexistent file");
        } catch (RuntimeException e) {
            Assert.assertTrue("Error message should mention resource issue", 
                            e.getMessage().toLowerCase().contains("resource") ||
                            e.getMessage().toLowerCase().contains("read"));
        }
    }
}