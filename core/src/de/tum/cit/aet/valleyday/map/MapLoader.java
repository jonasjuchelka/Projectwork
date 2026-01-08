package de.tum.cit.aet.valleyday.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapLoader {
    private static final int DEFAULT_WIDTH = 21;
    private static final int DEFAULT_HEIGHT = 21;
    private static final int EMPTY = -1;
    private static final Pattern ROW_KEY = Pattern.compile("row(\\d+)", Pattern.CASE_INSENSITIVE);

    private MapLoader() {}

    public static int[][] load(FileHandle file) {
        return load(file, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public static int[][] load(FileHandle file, int width, int height) {
        if (file == null || !file.exists()) {
            Gdx.app.log("MapLoad", "File not found: " + file);
            return null;
        }
        return parseContent(file.readString(), width, height);
    }

    public static int[][] load(String content, int width, int height) {
        return parseContent(content, width, height);
    }

    private static int[][] parseContent(String content, int defaultWidth, int defaultHeight) {
        if (defaultWidth <= 0 || defaultHeight <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }

        Map<Integer, String> rowEntries = new HashMap<>();
        Map<Point, Integer> coordEntries = new HashMap<>();
        int targetWidth = defaultWidth;
        int targetHeight = defaultHeight;

        if (content == null) {
            content = "";
        }

        String[] lines = content.split("\\r?\\n");
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String[] parts = line.split("=", 2);
            if (parts.length != 2) {
                continue;
            }

            String key = parts[0].trim();
            String value = parts[1].trim();

            if (key.equalsIgnoreCase("width")) {
                targetWidth = clampPositive(value, defaultWidth);
                continue;
            }

            if (key.equalsIgnoreCase("height")) {
                targetHeight = clampPositive(value, defaultHeight);
                continue;
            }

            Matcher rowMatcher = ROW_KEY.matcher(key);
            if (rowMatcher.matches()) {
                int rowIndex = parseInt(rowMatcher.group(1), -1);
                if (rowIndex >= 0 && rowIndex < defaultHeight) {
                    rowEntries.put(rowIndex, value);
                    targetHeight = Math.max(targetHeight, rowIndex + 1);
                }
                continue;
            }

            String[] coords = key.split(",");
            if (coords.length == 2) {
                int x = parseInt(coords[0], Integer.MIN_VALUE);
                int y = parseInt(coords[1], Integer.MIN_VALUE);
                int type = parseInt(value, Integer.MIN_VALUE);
                if (x != Integer.MIN_VALUE && y != Integer.MIN_VALUE && type != Integer.MIN_VALUE) {
                    coordEntries.put(new Point(x, y), type);
                    targetWidth = Math.max(targetWidth, x + 1);
                    targetHeight = Math.max(targetHeight, y + 1);
                }
            }
        }

        targetWidth = Math.min(targetWidth, defaultWidth);
        targetHeight = Math.min(targetHeight, defaultHeight);

        int[][] grid = new int[targetWidth][targetHeight];
        for (int x = 0; x < targetWidth; x++) {
            Arrays.fill(grid[x], EMPTY);
        }

        for (Map.Entry<Integer, String> entry : rowEntries.entrySet()) {
            int y = entry.getKey();
            if (y < 0 || y >= targetHeight) {
                continue;
            }

            String row = entry.getValue();
            for (int x = 0; x < Math.min(row.length(), targetWidth); x++) {
                int type = charToType(row.charAt(x));
                grid[x][y] = type;
            }
        }

        for (Map.Entry<Point, Integer> entry : coordEntries.entrySet()) {
            int x = entry.getKey().x;
            int y = entry.getKey().y;
            if (x >= 0 && x < targetWidth && y >= 0 && y < targetHeight) {
                grid[x][y] = entry.getValue();
            }
        }

        return grid;
    }

    private static int charToType(char c) {
        if (c == '.' || c == ' ') {
            return EMPTY;
        }
        if (Character.isDigit(c)) {
            return Character.digit(c, 10);
        }
        return EMPTY;
    }

    private static int clampPositive(String value, int fallback) {
        int parsed = parseInt(value, fallback);
        return parsed > 0 ? parsed : fallback;
    }

    private static int parseInt(String text, int fallback) {
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private record Point(int x, int y) { }
}
