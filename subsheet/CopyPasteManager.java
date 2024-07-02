package subsheet;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CopyPasteManager {
    private static String copiedText;
    private static CellCode copiedCell;

    public static void setCopiedText(CellCode copiedCell, String copiedText) {
        CopyPasteManager.copiedCell = copiedCell;
        CopyPasteManager.copiedText = copiedText;
    }

    private static String replaceCellReferences(CellCode pastedCell, MatchResult result) {
        CellCode difference = pastedCell.difference(copiedCell);
        CellCode newCellCode = CellCode.fromString(result.group()).add(difference);
        return Matcher.quoteReplacement(newCellCode.toString());
    }

    // TODO: Fix replacing in strings
    public static String getCopiedText(CellCode pastedCell) {
        String resultText = copiedText;
        if (copiedText.startsWith("=")) {
            Matcher matcher = Pattern.compile("[A-Z]+\\d+").matcher(copiedText);
            resultText = matcher.replaceAll(r -> replaceCellReferences(pastedCell, r));
        }
        return resultText;
    }

    public static boolean hasCopiedText() {
        return copiedText != null;
    }
}
