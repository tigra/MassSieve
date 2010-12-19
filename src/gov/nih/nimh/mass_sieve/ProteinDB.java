package gov.nih.nimh.mass_sieve;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alex Turbin (alex.academATgmail.com)
 */
public class ProteinDB {

    public static ProteinDB Instance = new ProteinDB();
    private Map<String, ProteinInfo> map = new HashMap<String, ProteinInfo>();

    private ProteinDB() {
        // singleton
    }

    public ProteinInfo get(String name) {
        return map.get(name);
    }

    public void add(ProteinInfo info) {
        map.put(info.getName(), info);
    }

    public Iterable<String> proteinNames() {
        return map.keySet();
    }

    public Map<String, ProteinInfo> getMap() {
        return Collections.unmodifiableMap(map);
    }

    public boolean contains(String name) {
        return map.containsKey(name);
    }
}
