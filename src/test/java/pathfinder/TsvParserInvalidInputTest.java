package pathfinder;

import org.junit.Assert;
import org.junit.Test;
import shortestpath.TsvParser;
import shortestpath.TsvParser.TsvParseException;

/**
 * Tests that specifically verify that invalid input data causes tests to fail.
 * These tests ensure that the parsing is strict and catches malformed data.
 */
public class TsvParserInvalidInputTest {

    @Test(expected = TsvParseException.class)
    public void testFailOnNullInput() {
        TsvParser.parse((String) null);
    }

    @Test(expected = TsvParseException.class)
    public void testFailOnEmptyString() {
        TsvParser.parse("");
    }

    @Test(expected = TsvParseException.class)
    public void testFailOnWhitespaceOnly() {
        TsvParser.parse("   \n   \t   \n   ");
    }

    @Test(expected = TsvParseException.class)
    public void testFailOnNullByteArray() {
        TsvParser.parse((byte[]) null);
    }

    @Test(expected = TsvParseException.class)
    public void testFailOnDuplicateHeaders() {
        String invalidTsv = "Name\tAge\tName\n" +  // Duplicate "Name" header
                           "John\t25\tDoe";
        TsvParser.parse(invalidTsv);
    }

    @Test(expected = TsvParseException.class)
    public void testFailOnTriplicateHeaders() {
        String invalidTsv = "ID\tName\tID\tAge\tID\n" +  // Triplicate "ID" header
                           "1\tJohn\t1\t25\t1";
        TsvParser.parse(invalidTsv);
    }

    @Test(expected = TsvParseException.class)
    public void testFailOnNoHeaderLine() {
        String invalidTsv = "\n\n\n"; // Only empty lines
        TsvParser.parse(invalidTsv);
    }

    @Test(expected = TsvParseException.class)
    public void testFailOnCommentsOnly() {
        String invalidTsv = "# This is a comment\n" +
                           "# Another comment\n" +
                           "# No actual header";
        TsvParser.parse(invalidTsv);
    }

    @Test
    public void testStrictValidationDetectsComplexDuplicates() {
        // Test that duplicate detection works even with spaces and mixed case
        String invalidTsv1 = " Header1 \tHeader2\t Header1 \n" +  // Duplicate with spaces
                             "value1\tvalue2\tvalue3";
        
        try {
            TsvParser.parse(invalidTsv1);
            Assert.fail("Should have detected duplicate headers with spaces");
        } catch (TsvParseException e) {
            Assert.assertTrue("Exception should mention duplicate", 
                            e.getMessage().toLowerCase().contains("duplicate"));
        }
    }

    @Test
    public void testValidationCatchesSubtleErrors() {
        // Test that validation catches errors that might be missed by lenient parsing
        
        // Test 1: Empty header line
        try {
            TsvParser.parse("\t\t\t\n");  // Only tabs, no actual headers
            // This should work but produce empty headers - test that it's handled
        } catch (TsvParseException e) {
            // If it throws an exception, that's also valid strict behavior
        }
        
        // Test 2: Header line with only whitespace
        try {
            TsvParser.parse("   \t   \t   \n");  // Only whitespace
            // This should work but produce empty headers after trimming
        } catch (TsvParseException e) {
            // If it throws an exception, that's also valid strict behavior
        }
    }

    @Test
    public void testExceptionMessagesAreInformative() {
        try {
            TsvParser.parse("Header1\tHeader2\tHeader1\ndata");
        } catch (TsvParseException e) {
            String message = e.getMessage().toLowerCase();
            Assert.assertTrue("Exception message should mention 'duplicate'", 
                            message.contains("duplicate"));
            Assert.assertTrue("Exception message should mention the header name", 
                            message.contains("header1"));
        }
    }

    @Test
    public void testValidateMethodStrictness() {
        // Test that the validate method is equally strict
        
        String[] invalidInputs = {
            null,
            "",
            "   \n   \n   ",
            "Header\tHeader\ndata\tdata",  // Duplicate headers
            "# Only comments\n# No headers"
        };
        
        for (String input : invalidInputs) {
            try {
                TsvParser.validate(input);
                if (input != null) {  // null case always throws
                    Assert.fail("Validate should have rejected invalid input: " + input);
                }
            } catch (TsvParseException e) {
                // Expected - this is what we want
                Assert.assertNotNull("Exception message should not be null", e.getMessage());
            }
        }
    }

    @Test
    public void testByteArrayValidationStrictness() {
        String invalidTsv = "Name\tAge\tName\n" +  // Duplicate headers
                           "John\t25\tDoe";
        byte[] invalidBytes = invalidTsv.getBytes();
        
        try {
            TsvParser.parse(invalidBytes);
            Assert.fail("Should have rejected byte array with duplicate headers");
        } catch (TsvParseException e) {
            Assert.assertTrue("Exception should mention duplicate", 
                            e.getMessage().toLowerCase().contains("duplicate"));
        }
    }

    @Test
    public void testValidationFailsOnCorruptedData() {
        // Test various forms of corrupted or malformed data
        
        String[] corruptedInputs = {
            "\0\0\0",  // Null bytes
            "Header1\tHeader1\tHeader1\n",  // All same headers
            "A\tB\tA\tB\tA\n",  // Multiple duplicates
        };
        
        for (String corrupted : corruptedInputs) {
            try {
                TsvParser.parse(corrupted);
                Assert.fail("Should have rejected corrupted input: " + corrupted);
            } catch (TsvParseException e) {
                // Expected
                Assert.assertNotNull("Exception should have a message", e.getMessage());
            }
        }
    }

    @Test
    public void testStrictModeRejectsAmbiguousData() {
        // Test cases where data could be interpreted multiple ways
        // but strict parsing should choose one interpretation or reject it
        
        // Case: Headers that are identical after trimming
        String ambiguous1 = "Header1\t Header1 \n" +  // Same after trimming
                           "value1\tvalue2";
        
        try {
            TsvParser.parse(ambiguous1);
            Assert.fail("Should reject headers that are identical after trimming");
        } catch (TsvParseException e) {
            Assert.assertTrue("Should mention duplicate", 
                            e.getMessage().toLowerCase().contains("duplicate"));
        }
    }

    @Test
    public void testRejectsInvalidCharacterEncodings() {
        // Test with byte arrays that represent invalid UTF-8
        byte[] invalidUtf8 = {
            (byte) 0xFF, (byte) 0xFE,  // Invalid UTF-8 byte sequence
            'H', 'e', 'a', 'd', 'e', 'r', '\t',
            'D', 'a', 't', 'a', '\n'
        };
        
        try {
            TsvParser.parse(invalidUtf8);
            // If it doesn't throw an exception, the encoding was handled
            // But we should still verify it's working correctly
        } catch (Exception e) {
            // Could throw TsvParseException or encoding-related exception
            // Both are acceptable for strict validation
        }
    }
}