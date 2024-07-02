package subsheet;

import java.io.Serializable;

public class ProjectData implements Serializable {
    Spreadsheet spreadsheet;

    public void onDeserialize() {
        spreadsheet.onDeserialize();
    }
}
