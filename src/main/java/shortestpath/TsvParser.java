package shortestpath;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import lombok.extern.slf4j.Slf4j;

/**
 * A strict TSV (Tab-Separated Values) parser that validates format and structure.
 * Handles header parsing, comment filtering, and field validation.
 */
@Slf4j
public class TsvParser {
    private static final String DELIM_COLUMN = "\t";
    private static final String PREFIX_COMMENT = "#";

    /**
     * Represents a parsed TSV data structure with headers and rows.
     */
    public static class TsvData {
        private final String[] headers;
        private final List<Map<String, String>> rows;

        public TsvData(String[] headers, List<Map<String, String>> rows) {
            this.headers = headers.clone();
            this.rows = new ArrayList<>(rows);
        }

        public String[] getHeaders() {
            return headers.clone();
        }

        public List<Map<String, String>> getRows() {
            return new ArrayList<>(rows);
        }

        public int getRowCount() {
            return rows.size();
        }

        public int getColumnCount() {
            return headers.length;
        }
    }

    /**
     * Exception thrown when TSV parsing encounters invalid data.
     */
    public static class TsvParseException extends RuntimeException {
        public TsvParseException(String message) {
            super(message);
        }

        public TsvParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Parses TSV content from a string with strict validation.
     * 
     * @param content The raw TSV content as a string
     * @return TsvData containing parsed headers and rows
     * @throws TsvParseException if the TSV format is invalid
     */
    public static TsvData parse(String content) throws TsvParseException {
        if (content == null || content.trim().isEmpty()) {
            throw new TsvParseException("TSV content cannot be null or empty");
        }

        Scanner scanner = new Scanner(content);
        String[] headers = null;
        List<Map<String, String>> rows = new ArrayList<>();
        int lineNumber = 0;

        try {
            // Parse header line
            while (scanner.hasNextLine()) {
                lineNumber++;
                String line = scanner.nextLine();

                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                // Process header line (first non-empty line)
                if (headers == null) {
                    headers = parseHeaderLine(line, lineNumber);
                    break;
                }
            }

            if (headers == null) {
                throw new TsvParseException("No header line found in TSV content");
            }

            // Parse data lines
            while (scanner.hasNextLine()) {
                lineNumber++;
                String line = scanner.nextLine();

                // Skip comments and empty lines
                if (line.startsWith(PREFIX_COMMENT) || line.trim().isEmpty()) {
                    continue;
                }

                Map<String, String> row = parseDataLine(line, headers, lineNumber);
                rows.add(row);
            }

            return new TsvData(headers, rows);

        } catch (Exception e) {
            throw new TsvParseException("Error parsing TSV at line " + lineNumber + ": " + e.getMessage(), e);
        } finally {
            scanner.close();
        }
    }

    /**
     * Parses TSV content from a byte array.
     * 
     * @param bytes The raw TSV content as bytes
     * @return TsvData containing parsed headers and rows
     * @throws TsvParseException if the TSV format is invalid
     */
    public static TsvData parse(byte[] bytes) throws TsvParseException {
        if (bytes == null) {
            throw new TsvParseException("TSV bytes cannot be null");
        }
        String content = new String(bytes, StandardCharsets.UTF_8);
        return parse(content);
    }

    /**
     * Parses the header line and validates it.
     */
    private static String[] parseHeaderLine(String line, int lineNumber) throws TsvParseException {
        // Handle header line that may start with comment prefix
        String headerLine = line;
        if (headerLine.startsWith(PREFIX_COMMENT + " ")) {
            headerLine = headerLine.replace(PREFIX_COMMENT + " ", "");
        } else if (headerLine.startsWith(PREFIX_COMMENT)) {
            headerLine = headerLine.replace(PREFIX_COMMENT, "");
        }

        String[] headers = headerLine.split(DELIM_COLUMN, -1); // -1 to include trailing empty strings

        // Validate headers
        if (headers.length == 0) {
            throw new TsvParseException("Header line cannot be empty at line " + lineNumber);
        }

        for (int i = 0; i < headers.length; i++) {
            if (headers[i] == null) {
                throw new TsvParseException("Header at column " + (i + 1) + " cannot be null at line " + lineNumber);
            }
            // Trim whitespace from headers
            headers[i] = headers[i].trim();
            
            // Check for duplicate headers
            for (int j = i + 1; j < headers.length; j++) {
                if (headers[i].equals(headers[j]) && !headers[i].isEmpty()) {
                    throw new TsvParseException("Duplicate header '" + headers[i] + "' found at line " + lineNumber);
                }
            }
        }

        return headers;
    }

    /**
     * Parses a data line and validates it against headers.
     */
    private static Map<String, String> parseDataLine(String line, String[] headers, int lineNumber) throws TsvParseException {
        String[] fields = line.split(DELIM_COLUMN, -1); // -1 to include trailing empty strings
        Map<String, String> fieldMap = new HashMap<>();

        // Create field map, allowing for missing trailing fields
        for (int i = 0; i < headers.length; i++) {
            String value = "";
            if (i < fields.length) {
                value = fields[i];
            }
            fieldMap.put(headers[i], value);
        }

        // Warn if there are more fields than headers (potential data corruption)
        if (fields.length > headers.length) {
            log.warn("Line {} has {} fields but only {} headers. Extra fields will be ignored.", 
                    lineNumber, fields.length, headers.length);
        }

        return fieldMap;
    }

    /**
     * Validates that a TSV content string has proper structure.
     * 
     * @param content The TSV content to validate
     * @throws TsvParseException if validation fails
     */
    public static void validate(String content) throws TsvParseException {
        // This will throw TsvParseException if invalid
        parse(content);
    }

    /**
     * Validates that a TSV byte array has proper structure.
     * 
     * @param bytes The TSV content to validate
     * @throws TsvParseException if validation fails
     */
    public static void validate(byte[] bytes) throws TsvParseException {
        // This will throw TsvParseException if invalid
        parse(bytes);
    }
}