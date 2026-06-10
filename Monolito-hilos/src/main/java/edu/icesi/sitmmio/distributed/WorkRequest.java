package edu.icesi.sitmmio.distributed;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class WorkRequest implements Serializable {
    private final List<Map<String, String>> rows;
    private final Set<String> activeRoutes;

    public WorkRequest(List<Map<String, String>> rows, Set<String> activeRoutes) {
        this.rows = rows;
        this.activeRoutes = activeRoutes;
    }

    public List<Map<String, String>> getRows() {
        return rows;
    }

    public Set<String> getActiveRoutes() {
        return activeRoutes;
    }
}
