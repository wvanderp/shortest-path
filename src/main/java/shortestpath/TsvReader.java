package shortestpath;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Utility class to read TSV/CSV files from resources and return a list of field
 * maps.
 */
public class TsvReader {
    private static final String DELIM_COLUMN = "\t";
    private static final String PREFIX_COMMENT = "#";

    /**
     * Parses TSV/CSV content and returns a list of field maps (header -> value).
     * Skips comment and blank lines.
     *
     * @param content file content as String
     * @return list of field maps
     */
    public static List<Map<String, String>> parseResource(String content) {
        Scanner scanner = new Scanner(content);
        // Header line is the first line in the file and will start with either '#' or '# '
        String[] headers = scanner
            .nextLine()
            .replaceFirst("^# ?", "")
            .split(DELIM_COLUMN);

        List<Map<String, String>> rows = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith(PREFIX_COMMENT) || line.isBlank()) {
                continue;
            }
            String[] fields = line.split(DELIM_COLUMN);
            Map<String, String> fieldMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                if (i < fields.length) {
                    fieldMap.put(headers[i], fields[i]);
                }
            }
            rows.add(fieldMap);
        }
        scanner.close();
        return rows;
    }

    /**
     * Reads a TSV/CSV resource and returns a list of field maps (header -> value).
     * Skips comment and blank lines.
     *
     * @param path resource path
     * @return list of field maps
     * @throws IOException if resource cannot be read
     */
    public static List<Map<String, String>> readResource(String path) throws IOException {
        String content = new String(Util.readAllBytes(ShortestPathPlugin.class.getResourceAsStream(path)),
                StandardCharsets.UTF_8);
        return parseResource(content);
    }
}
