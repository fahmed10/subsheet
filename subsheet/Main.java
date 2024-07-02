package subsheet;

import java.awt.*;
import javax.swing.*;

public class Main {
    private static JFrame frame;
    private static Spreadsheet currentSpreadsheet;
    public static String savePath;

    public static void main(String[] args) {
        frame = new JFrame("Subsheet");
        currentSpreadsheet = new Spreadsheet(40, 26);
        currentSpreadsheet.addToFrame(frame);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setJMenuBar(new AppMenuBar());
        frame.setSize(720, 480);
        frame.setMinimumSize(new Dimension(720 / 2, 480 / 2));
        frame.setLayout(new GridLayout());
        frame.setVisible(true);
    }

    public static void loadSpreadsheet(Spreadsheet spreadsheet) {
        currentSpreadsheet.removeFromFrame(frame);
        currentSpreadsheet = spreadsheet != null ? spreadsheet : new Spreadsheet(40, 26);
        currentSpreadsheet.addToFrame(frame);
        SwingUtilities.updateComponentTreeUI(frame);
    }

    public static ProjectData getProjectObject() {
        ProjectData data = new ProjectData();
        currentSpreadsheet.preSave();
        data.spreadsheet = currentSpreadsheet;
        return data;
    }

    public static void loadProjectObject(ProjectData projectData, String filePath) {
        projectData.onDeserialize();
        savePath = filePath;
        loadSpreadsheet(projectData.spreadsheet);
    }

    public static Spreadsheet getCurrentSpreadsheet() {
        return currentSpreadsheet;
    }
}