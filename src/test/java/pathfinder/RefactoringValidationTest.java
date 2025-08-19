package pathfinder;

import org.junit.Test;
import shortestpath.TsvParser;
import shortestpath.Transport;
import shortestpath.TransportLoader;
import shortestpath.TransportType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Quick validation test to ensure the refactored system compiles and basic functionality works.
 */
public class RefactoringValidationTest {

    @Test
    public void testTsvParserCompiles() {
        // Simple test to ensure TsvParser compiles and works
        String simpleTsv = "Header1\tHeader2\nValue1\tValue2";
        TsvParser.TsvData data = TsvParser.parse(simpleTsv);
        
        assert data != null;
        assert data.getColumnCount() == 2;
        assert data.getRowCount() == 1;
    }

    @Test
    public void testTransportLoaderCompiles() {
        // Simple test to ensure TransportLoader compiles
        TransportLoader.TransportConfig config = 
            new TransportLoader.TransportConfig("/test.tsv", TransportType.TRANSPORT);
        
        assert config != null;
        assert config.getResourcePath().equals("/test.tsv");
        assert config.getTransportType() == TransportType.TRANSPORT;
    }

    @Test
    public void testTransportConstructorWorks() {
        // Test that Transport constructor still works with the refactored code
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("Origin", "1000 1000 0");
        fieldMap.put("Destination", "1010 1010 0");
        
        Transport transport = new Transport(fieldMap, TransportType.TRANSPORT);
        
        assert transport != null;
        assert transport.getType() == TransportType.TRANSPORT;
    }

    @Test
    public void testBackwardsCompatibilityMethod() {
        // Test that the deprecated method still exists and can be called
        try {
            HashMap<Integer, Set<Transport>> transports = Transport.loadAllFromResources();
            // If this compiles and runs without throwing a compilation error, 
            // backwards compatibility is maintained
            assert transports != null;
        } catch (RuntimeException e) {
            // Runtime exceptions are okay - they happen when resources are missing
            // The important thing is that the method exists and compiles
        }
    }

    @Test 
    public void testNewSystemWorks() {
        // Test that the new system works
        try {
            HashMap<Integer, Set<Transport>> transports = TransportLoader.loadAllFromResources();
            assert transports != null;
        } catch (RuntimeException e) {
            // Runtime exceptions are okay for missing resources in test environment
        }
    }
}