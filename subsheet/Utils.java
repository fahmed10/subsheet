package subsheet;

public class Utils {
    public static Double tryParseDouble(String s) {
        try {
            return Double.parseDouble(s);
        }
        catch (NumberFormatException ex) {
            return null;
        }
    }
}
