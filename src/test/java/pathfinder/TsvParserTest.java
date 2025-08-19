package pathfinder;

import org.junit.Assert;
import org.junit.Test;
import shortestpath.TsvParser;
import shortestpath.TsvParser.TsvData;
import shortestpath.TsvParser.TsvParseException;

import java.util.List;
import java.util.Map;

public class TsvParserTest {

    @Test
    public void testValidTsvParsing() {
        String validTsv = "# Header1\tHeader2\tHeader3\n" +
                         "value1\tvalue2\tvalue3\n" +
                         "data1\tdata2\tdata3";
        
        TsvData result = TsvParser.parse(validTsv);
        
        Assert.assertEquals(3, result.getColumnCount());
        Assert.assertEquals(2, result.getRowCount());
        
        String[] headers = result.getHeaders();
        Assert.assertEquals("Header1", headers[0]);
        Assert.assertEquals("Header2", headers[1]);
        Assert.assertEquals("Header3", headers[2]);
        
        List<Map<String, String>> rows = result.getRows();
        Assert.assertEquals("value1", rows.get(0).get("Header1"));
        Assert.assertEquals("value2", rows.get(0).get("Header2"));
        Assert.assertEquals("value3", rows.get(0).get("Header3"));
        Assert.assertEquals("data1", rows.get(1).get("Header1"));
        Assert.assertEquals("data2", rows.get(1).get("Header2"));
        Assert.assertEquals("data3", rows.get(1).get("Header3"));
    }

    @Test
    public void testHeaderWithCommentSpace() {
        String tsvWithCommentSpace = "# \tHeader1\tHeader2\n" +
                                    "value1\tvalue2";
        
        TsvData result = TsvParser.parse(tsvWithCommentSpace);
        
        String[] headers = result.getHeaders();
        Assert.assertEquals("Header1", headers[0]);
        Assert.assertEquals("Header2", headers[1]);
    }

    @Test
    public void testSkipCommentsAndEmptyLines() {
        String tsvWithComments = "# Header1\tHeader2\n" +
                                "# This is a comment\n" +
                                "\n" +
                                "value1\tvalue2\n" +
                                "# Another comment\n" +
                                "data1\tdata2\n" +
                                "\n";
        
        TsvData result = TsvParser.parse(tsvWithComments);
        
        Assert.assertEquals(2, result.getRowCount());
        List<Map<String, String>> rows = result.getRows();
        Assert.assertEquals("value1", rows.get(0).get("Header1"));
        Assert.assertEquals("data1", rows.get(1).get("Header1"));
    }

    @Test
    public void testMissingTrailingFields() {
        String tsvMissingFields = "Header1\tHeader2\tHeader3\n" +
                                 "value1\tvalue2\n" +  // Missing third field
                                 "data1\tdata2\tdata3";
        
        TsvData result = TsvParser.parse(tsvMissingFields);
        
        List<Map<String, String>> rows = result.getRows();
        Assert.assertEquals("value1", rows.get(0).get("Header1"));
        Assert.assertEquals("value2", rows.get(0).get("Header2"));
        Assert.assertEquals("", rows.get(0).get("Header3")); // Should be empty string
    }

    @Test
    public void testExtraFields() {
        String tsvExtraFields = "Header1\tHeader2\n" +
                               "value1\tvalue2\textra\tanother"; // Extra fields
        
        // Should not throw exception, but extra fields are ignored
        TsvData result = TsvParser.parse(tsvExtraFields);
        
        Assert.assertEquals(2, result.getColumnCount());
        List<Map<String, String>> rows = result.getRows();
        Assert.assertEquals("value1", rows.get(0).get("Header1"));
        Assert.assertEquals("value2", rows.get(0).get("Header2"));
    }

    @Test(expected = TsvParseException.class)
    public void testNullContent() {
        TsvParser.parse((String) null);
    }

    @Test(expected = TsvParseException.class)
    public void testEmptyContent() {
        TsvParser.parse("");
    }

    @Test(expected = TsvParseException.class)
    public void testWhitespaceOnlyContent() {
        TsvParser.parse("   \n  \n  ");
    }

    @Test(expected = TsvParseException.class)
    public void testNullBytes() {
        TsvParser.parse((byte[]) null);
    }

    @Test(expected = TsvParseException.class)
    public void testDuplicateHeaders() {
        String invalidTsv = "Header1\tHeader2\tHeader1\n" + // Duplicate Header1
                           "value1\tvalue2\tvalue3";
        
        TsvParser.parse(invalidTsv);
    }

    @Test
    public void testEmptyHeaders() {
        String tsvEmptyHeaders = "Header1\t\tHeader3\n" +
                                "value1\tvalue2\tvalue3";
        
        // Should parse without throwing exception (empty headers are allowed)
        TsvData result = TsvParser.parse(tsvEmptyHeaders);
        
        String[] headers = result.getHeaders();
        Assert.assertEquals("Header1", headers[0]);
        Assert.assertEquals("", headers[1]);
        Assert.assertEquals("Header3", headers[2]);
    }

    @Test
    public void testValidateMethod() {
        String validTsv = "Header1\tHeader2\n" +
                         "value1\tvalue2";
        
        // Should not throw exception
        TsvParser.validate(validTsv);
    }

    @Test(expected = TsvParseException.class)
    public void testValidateInvalidTsv() {
        String invalidTsv = "Header1\tHeader2\tHeader1\n" + // Duplicate headers
                           "value1\tvalue2\tvalue3";
        
        TsvParser.validate(invalidTsv);
    }

    @Test
    public void testByteArrayParsing() {
        String validTsv = "Header1\tHeader2\n" +
                         "value1\tvalue2";
        byte[] bytes = validTsv.getBytes();
        
        TsvData result = TsvParser.parse(bytes);
        
        Assert.assertEquals(2, result.getColumnCount());
        Assert.assertEquals(1, result.getRowCount());
    }

    @Test
    public void testHeaderTrimming() {
        String tsvWithSpaces = " Header1 \t Header2 \t Header3 \n" +
                              "value1\tvalue2\tvalue3";
        
        TsvData result = TsvParser.parse(tsvWithSpaces);
        
        String[] headers = result.getHeaders();
        Assert.assertEquals("Header1", headers[0]);
        Assert.assertEquals("Header2", headers[1]);
        Assert.assertEquals("Header3", headers[2]);
    }

    @Test
    public void testEmptyDataRows() {
        String tsvEmptyRows = "Header1\tHeader2\n" +
                             "\t\n" +  // Empty row
                             "value1\tvalue2";
        
        TsvData result = TsvParser.parse(tsvEmptyRows);
        
        Assert.assertEquals(2, result.getRowCount());
        List<Map<String, String>> rows = result.getRows();
        Assert.assertEquals("", rows.get(0).get("Header1"));
        Assert.assertEquals("", rows.get(0).get("Header2"));
        Assert.assertEquals("value1", rows.get(1).get("Header1"));
    }

    @Test
    public void testSingleColumnTsv() {
        String singleColumn = "Header1\n" +
                             "value1\n" +
                             "value2";
        
        TsvData result = TsvParser.parse(singleColumn);
        
        Assert.assertEquals(1, result.getColumnCount());
        Assert.assertEquals(2, result.getRowCount());
        
        List<Map<String, String>> rows = result.getRows();
        Assert.assertEquals("value1", rows.get(0).get("Header1"));
        Assert.assertEquals("value2", rows.get(1).get("Header1"));
    }

    @Test
    public void testTabsInData() {
        String tsvWithTabs = "Header1\tHeader2\n" +
                            "value\twith\ttabs\tvalue2";
        
        TsvData result = TsvParser.parse(tsvWithTabs);
        
        List<Map<String, String>> rows = result.getRows();
        Assert.assertEquals("value", rows.get(0).get("Header1"));
        Assert.assertEquals("with", rows.get(0).get("Header2"));
        // Extra tabs create additional fields that are ignored in this case
    }
}