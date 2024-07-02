package sil;

import subsheet.CellCode;

public record SILRange(CellCode start, CellCode end) {
    public CellCode[] getCellsInRange() {
        return CellCode.getRange(start, end);
    }
}
