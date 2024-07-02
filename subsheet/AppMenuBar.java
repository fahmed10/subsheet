package subsheet;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;

public class AppMenuBar extends JMenuBar {
    private static final FileFilter spreadsheetFileFilter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            return f.getName().endsWith(".ss");
        }

        @Override
        public String getDescription() {
            return "subsheet.subsheet.Spreadsheet Files (*.ss)";
        }
    };

    public AppMenuBar() {
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.add(createMenu("New", this::onNew));
        fileMenu.add(createMenu("Save", this::onSave, "ctrl S"));
        fileMenu.add(createMenu("Save As", this::onSaveAs));
        fileMenu.add(createMenu("Open", this::onOpen));
        add(fileMenu);

        JMenu cellMenu = new JMenu("Cell");
        cellMenu.setMnemonic(KeyEvent.VK_C);
        cellMenu.add(createMenu("Format Plain", e -> formatCell(Font.PLAIN)));
        cellMenu.add(createMenu("Format Bold", e -> formatCell(Font.BOLD), "ctrl B"));
        cellMenu.add(createMenu("Format Italic", e -> formatCell(Font.ITALIC), "ctrl I"));
        add(cellMenu);
    }

    private JMenuItem createMenu(String text, ActionListener actionListener) {
        JMenuItem menuItem = new JMenuItem(text);
        menuItem.addActionListener(actionListener);
        return menuItem;
    }

    private JMenuItem createMenu(String text, ActionListener actionListener, String shortcut) {
        JMenuItem menuItem = new JMenuItem(text);
        menuItem.addActionListener(actionListener);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(shortcut));
        return menuItem;
    }

    private void formatCell(int style) {
        Main.getCurrentSpreadsheet().getSelectedCell().setStyle(style);
    }

    private void onNew(ActionEvent actionEvent) {
        Main.loadSpreadsheet(null);
    }

    private void onSave(ActionEvent actionEvent) {
        if (Main.savePath == null) {
            onSaveAs(actionEvent);
            return;
        }

        try {
            FileOutputStream fileStream = new FileOutputStream(Main.savePath);
            ObjectOutputStream stream = new ObjectOutputStream(fileStream);
            stream.writeObject(Main.getProjectObject());
            stream.close();
            fileStream.close();
        } catch (IOException ex) {
            System.out.println("IOException: " + ex.getMessage());
        }
    }

    private void onSaveAs(ActionEvent actionEvent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(spreadsheetFileFilter);
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String filePath = file.getAbsolutePath();
            if (!file.getName().endsWith(".ss")) {
                filePath += ".ss";
            }
            try {
                FileOutputStream fileStream = new FileOutputStream(filePath);
                ObjectOutputStream stream = new ObjectOutputStream(fileStream);
                Main.savePath = filePath;
                stream.writeObject(Main.getProjectObject());
                stream.close();
                fileStream.close();
            } catch (IOException ex) {
                System.out.println("IOException: " + ex.getMessage());
            }
        }
    }

    private void onOpen(ActionEvent actionEvent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(spreadsheetFileFilter);
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                FileInputStream fileStream = new FileInputStream(file);
                ObjectInputStream stream = new ObjectInputStream(fileStream);
                Main.loadProjectObject((ProjectData) stream.readObject(), file.getAbsolutePath());
                stream.close();
                fileStream.close();
            } catch (IOException ex) {
                System.out.println("IOException: " + ex.getMessage());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
