package subsheet;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;

public class SpreadsheetCellEventHandler implements FocusListener, KeyListener {
    private final SpreadsheetCell cell;
    private final JTextField textField;

    public SpreadsheetCellEventHandler(SpreadsheetCell cell, JTextField textField) {
        this.cell = cell;
        this.textField = textField;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!cell.isEditable()) {
            return;
        }

        if (isCtrlShortcut(e, KeyEvent.VK_C) && textField.getSelectedText() == null) {
            CopyPasteManager.setCopiedText(cell.getCode(), textField.getText());
            e.consume();
        } else if (isCtrlShortcut(e, KeyEvent.VK_V) && textField.getSelectedText() == null && CopyPasteManager.hasCopiedText()) {
            textField.setText(CopyPasteManager.getCopiedText(cell.getCode()));
            e.consume();
        }
    }

    private boolean isCtrlShortcut(KeyEvent e, int key) {
        return e.getKeyCode() == key && (e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK;
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void focusGained(FocusEvent e) {
        if (!cell.isEditable()) {
            return;
        }

        cell.getSpreadsheet().setSelectedCell(cell);
        cell.getSpreadsheet().showEditingCell(cell.getCode());
        cell.setBorder(new LineBorder(new Color(0, 125, 220), 2));
        cell.updateDisplayText(true);
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (!cell.isEditable()) {
            return;
        }

        cell.setBorder(new LineBorder(Color.GRAY, 1));
        cell.updateDisplayText(false);
    }
}
