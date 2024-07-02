package subsheet;

import java.util.ArrayList;
import java.util.List;

public record CellCode(int x, int y) {
    public static CellCode fromString(String cellCode) {
        int x = cellCode.charAt(0) - 'A' + 1;
        int y = Integer.parseInt(String.valueOf(cellCode.charAt(1)));
        return new CellCode(x, y);
    }

    public static CellCode[] getRange(CellCode start, CellCode end) {
        List<CellCode> range = new ArrayList<>();

        for (int y = start.y(); y <= end.y(); y++) {
            for (int x = start.x(); x <= end.x(); x++) {
                range.add(new CellCode(x, y));
            }
        }

        return range.toArray(new CellCode[0]);
    }

    @Override
    public String toString() {
        return String.valueOf((char)('A' + x - 1)) + y;
    }

    public CellCode add(CellCode other) {
        return new CellCode(x + other.x, y + other.y);
    }

    public CellCode difference(CellCode other) {
        return new CellCode(x - other.x, y - other.y);
    }
}
