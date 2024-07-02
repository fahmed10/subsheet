package subsheet;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class Spreadsheet implements Serializable {
    private JPanel panel;
    private JScrollPane scrollPane;
    private List<SpreadsheetCell> cells = new ArrayList<>();
    private SpreadsheetCell selectedCell;
    private final int rows;
    private final int columns;

    public Spreadsheet(int rows, int columns) {
        rows++;
        columns++;
        this.rows = rows;
        this.columns = columns;
        panel = new JPanel(new GridBagLayout());
        scrollPane = new JScrollPane(panel);

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                if (x == 0 && y == 0) {
                    cells.add(new SpreadsheetCell(this, x, y, 1, true, ""));
                } else if (x == 0) {
                    cells.add(new SpreadsheetCell(this, x, y, 3, true, String.valueOf(y)));
                } else if (y == 0) {
                    cells.add(new SpreadsheetCell(this, x, y, 6, true, String.valueOf((char) ('A' + x - 1))));
                } else {
                    SpreadsheetCell cell = new SpreadsheetCell(this, x, y, 6);
                    cells.add(cell);
                }
            }
        }
    }

    public void onDeserialize() {
        for (SpreadsheetCell cell : cells) {
            cell.onDeserialize();
        }
    }

    public void preSave() {
        selectedCell.preSave();
    }

    public SpreadsheetCell getCellByCode(CellCode cellCode) {
        try {
            return cells.get(cellCode.y() * columns + cellCode.x());
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }

    public void showEditingCell(CellCode cellCode) {
        cells.get(0).overrideText(cellCode.toString());
    }

    public boolean refreshAllFormulaCells(SpreadsheetCell editedCell) {
        List<SpreadsheetCell> cells = topologicallySortedCells();

        if (cells == null) {
            return false;
        }

        for (SpreadsheetCell cell : cells) {
            if (cell.hasFormula()) {
                cell.calculateFormula();
            }
        }

        return true;
    }

    public List<SpreadsheetCell> topologicallySortedCells() {
        List<SpreadsheetCell> L = new ArrayList<>();
        List<SpreadsheetCell> S = new ArrayList<>();

        for (SpreadsheetCell cell : cells) {
            if (cell.hasFormula() && cell.timesReferenced() == 0) {
                S.add(cell);
            }
        }

        while (!S.isEmpty()) {
            SpreadsheetCell n = S.remove(0);
            L.add(n);
            for (SpreadsheetCell m : cells) {
                if (n.referencesCell(m.getCode())) {
                    n.removeReference(m.getCode());
                    if (m.timesReferenced() == 0) {
                        S.add(m);
                    }
                }
            }
        }

        if (anyReferences()) {
            return null;
        } else {
            return L;
        }
    }

    private boolean anyReferences() {
        for (SpreadsheetCell cell : cells) {
            if (cell.hasFormula() && cell.referenceCount() > 0) {
                return true;
            }
        }

        return false;
    }

    public SpreadsheetCell[] getAllCells() {
        return cells.toArray(new SpreadsheetCell[0]);
    }

    public void setSelectedCell(SpreadsheetCell selectedCell) {
        this.selectedCell = selectedCell;
    }

    public SpreadsheetCell getSelectedCell() {
        return selectedCell;
    }

    public void addToFrame(JFrame frame) {
        for (SpreadsheetCell cell : cells) {
            cell.addToPanel(panel);
        }

        frame.add(scrollPane);
    }

    public void removeFromFrame(JFrame frame) {
        frame.remove(scrollPane);
    }
}
