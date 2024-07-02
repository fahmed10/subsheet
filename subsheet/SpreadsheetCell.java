package subsheet;

import java.awt.*;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import sil.*;

public class SpreadsheetCell implements Serializable {
    private final Spreadsheet spreadsheet;
    private final JTextField textField;
    private final boolean header;
    private Font font;
    private String content;
    private String displayText;
    private final int x;
    private final int y;
    private HashSet<CellCode> references = new HashSet<>();

    public SpreadsheetCell(Spreadsheet spreadsheet, int x, int y, int size) {
        this(spreadsheet, x, y, size, false, "");
    }

    public SpreadsheetCell(Spreadsheet spreadsheet, int x, int y, int size, boolean header, String content) {
        this.spreadsheet = spreadsheet;
        this.x = x;
        this.y = y;
        this.header = header;

        textField = new JTextField(size);
        font = new Font("Arial", Font.PLAIN, 12);

        if (header) {
            textField.setHorizontalAlignment(SwingConstants.CENTER);
            font = font.deriveFont(Font.BOLD, 14);
            textField.setEditable(false);
            if (x == 0 && y == 0) {
                font = font.deriveFont(Font.ITALIC, 14);
            }
        }

        textField.setFont(font);
        this.content = content;
        this.displayText = content;
        textField.setText(content);
        textField.setBorder(new LineBorder(Color.GRAY, 1));

        SpreadsheetCellEventHandler eventHandler = new SpreadsheetCellEventHandler(this, textField);
        textField.addFocusListener(eventHandler);
        textField.addKeyListener(eventHandler);
    }

    public void onDeserialize() {
        SpreadsheetCellEventHandler eventHandler = new SpreadsheetCellEventHandler(this, textField);
        textField.addFocusListener(eventHandler);
        textField.addKeyListener(eventHandler);
    }

    public void setStyle(int style) {
        if (font.isPlain() && style != Font.PLAIN) {
            font = font.deriveFont(-1);
        }
        if (!font.isPlain() && style == Font.PLAIN) {
            font = font.deriveFont(-1);
        }
        font = font.deriveFont(font.getStyle() ^ style);
        textField.setFont(font);
    }

    public boolean isHeader() {
        return header;
    }

    public boolean isEditable() {
        return !header;
    }

    public void setBorder(Border border) {
        textField.setBorder(border);
    }

    public boolean hasFormula() {
        return content.startsWith("=");
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        updateDisplayText(false);
    }

    public String getDisplayText() {
        return displayText;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public CellCode getCode() {
        return new CellCode(x, y);
    }

    public Spreadsheet getSpreadsheet() {
        return spreadsheet;
    }

    public void overrideText(String text) {
        textField.setText(text);
    }

    public void updateDisplayText(boolean editing) {
        if (editing) {
            if (hasFormula()) {
                displayText = content;
            }
            content = textField.getText();
        } else {
            content = textField.getText();
            if (hasFormula()) {
                textField.setBackground(Color.CYAN);
                calculateFormula();
            } else {
                textField.setBackground(Color.WHITE);
                displayText = content;
            }

            if(!spreadsheet.refreshAllFormulaCells(this) && hasFormula()) {
                System.out.println("A");
                textField.setText("*CIRC_REF");
return;
            }
        }

        textField.setText(displayText);
    }

    public void preSave() {
        content = textField.getText();
    }

    public void calculateFormula() {
        SILTokenizer tokenizer = new SILTokenizer(content.substring(1));
        List<Token> tokens = tokenizer.tokenize();

        if (tokenizer.error != TokenizerError.NONE) {
            displayText = "#" + tokenizer.error;
            textField.setText(displayText);
            return;
        }

        references.clear();
        Stream<CellCode> cellReferences = tokens.stream().filter(t -> t.type() == TokenType.CELL)
                .map(t -> CellCode.fromString(t.lexeme())).distinct();
        references.addAll(cellReferences.toList());

        SILParser parser = new SILParser(tokens);
        Expression expression = parser.parse();

        if (parser.error != ParserError.NONE) {
            displayText = "#" + parser.error;
            textField.setText(displayText);
            return;
        }

        SILInterpreter interpreter = new SILInterpreter();
        Object result = interpreter.interpret(expression, new SILInterpreter.SpreadsheetInterface() {
            @Override
            public String getValueFromCell(String cellCode) {
                SpreadsheetCell cell;
                try {
                    cell = spreadsheet.getCellByCode(CellCode.fromString(cellCode));
                    if (cell == null) {
                        throw new NoSuchElementException();
                    }
                } catch (NoSuchElementException ex) {
                    throw new SILInterpreter.InterpreterException(InterpreterError.INV_CELL);
                }
                if (cell.hasFormula()) {
                    cell.calculateFormula();
                }
                return cell.getDisplayText();
            }

            @Override
            public String getValueFromCell(CellCode cellCode) {
                return getValueFromCell(cellCode.toString());
            }

            @Override
            public String getCellCode() {
                return getCode().toString();
            }
        });

        if (interpreter.error != InterpreterError.NONE) {
            displayText = "*" + interpreter.error;
            textField.setText(displayText);
            return;
        }

        if (result instanceof Double) {
            DecimalFormat formatter = new DecimalFormat("#.######");
            displayText = formatter.format(result);
        } else if (result instanceof Boolean b) {
            displayText = b ? "True" : "False";
        } else {
            displayText = result.toString();
        }

        textField.setText(displayText);
    }

    public boolean referencesCell(CellCode other) {
        return references.contains(other);
    }

    public void removeReference(CellCode other) {
        references.remove(other);
    }

    public int referenceCount() {
        return references.size();
    }

    public int timesReferenced() {
        int times = 0;

        for (SpreadsheetCell cell : spreadsheet.getAllCells()) {
            if (cell.referencesCell(getCode())) {
                times++;
            }
        }

        return times;
    }

    public void addToPanel(JPanel panel) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = x;
        constraints.gridy = y;
        if (!header) {
            constraints.weightx = 1;
            constraints.weighty = 1;
        }
        constraints.fill = GridBagConstraints.BOTH;
        panel.add(textField, constraints);
    }
}
