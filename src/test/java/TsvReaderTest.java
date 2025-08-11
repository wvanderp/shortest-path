
package shortestpath;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

public class TsvReaderTest {

	@Test
	public void testParseResource_basicTsv() {
		String tsv = "#col1\tcol2\tcol3\nval1\tval2\tval3\nval4\tval5\tval6\n";
		List<Map<String, String>> rows = TsvReader.parseResource(tsv);
		assertEquals(2, rows.size());
		assertEquals("val1", rows.get(0).get("col1"));
		assertEquals("val2", rows.get(0).get("col2"));
		assertEquals("val3", rows.get(0).get("col3"));
		assertEquals("val4", rows.get(1).get("col1"));
		assertEquals("val5", rows.get(1).get("col2"));
		assertEquals("val6", rows.get(1).get("col3"));
	}

	@Test
	public void testParseResource_skipsCommentsAndBlanks() {
		String tsv = "#col1\tcol2\n# this is a comment\nval1\tval2\n\nval3\tval4\n";
		List<Map<String, String>> rows = TsvReader.parseResource(tsv);
		assertEquals(2, rows.size());
		assertEquals("val1", rows.get(0).get("col1"));
		assertEquals("val2", rows.get(0).get("col2"));
		assertEquals("val3", rows.get(1).get("col1"));
		assertEquals("val4", rows.get(1).get("col2"));
	}

	@Test
	public void testParseResource_handlesHeaderWithHashSpace() {
		String tsv = "# col1\tcol2\nval1\tval2\n";
		List<Map<String, String>> rows = TsvReader.parseResource(tsv);
		assertEquals(1, rows.size());
		assertEquals("val1", rows.get(0).get("col1"));
		assertEquals("val2", rows.get(0).get("col2"));
	}

	@Test
	public void testParseResource_missingFields() {
		String tsv = "#col1\tcol2\tcol3\nval1\tval2\nval3\tval4\tval5\n";
		List<Map<String, String>> rows = TsvReader.parseResource(tsv);
		assertEquals(2, rows.size());
		assertEquals("val1", rows.get(0).get("col1"));
		assertEquals("val2", rows.get(0).get("col2"));
		assertNull(rows.get(0).get("col3"));
		assertEquals("val3", rows.get(1).get("col1"));
		assertEquals("val4", rows.get(1).get("col2"));
		assertEquals("val5", rows.get(1).get("col3"));
	}
}
