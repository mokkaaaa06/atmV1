package atm.repo.json;

import java.util.LinkedHashMap;
import java.util.Map;

public final class DefaultSeed {
    private DefaultSeed(){}

    public static Map<Integer,Integer> defaultEuroNotes() {
        Map<Integer,Integer> m = new LinkedHashMap<>();
        m.put(10000, 4);
        m.put(5000,  6);
        m.put(2000,  8);
        m.put(1000,  10);
        m.put(500,   10);
        return m;
    }

    public static int defaultPaper() { return 5; }
    public static int defaultInk() { return 5; }

    public static String technicianUsername() { return "tech"; }
    public static String technicianPassword() { return "0000"; }
}
