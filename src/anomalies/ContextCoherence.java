package anomalies;

import java.util.LinkedList;
import java.util.List;

public class ContextCoherence {
    private List<ContextComparison> supporters;
    private List<ContextComparison> anomalies;

    public ContextCoherence() {
        supporters = new LinkedList<>();
        anomalies = new LinkedList<>();
    }

    public List<ContextComparison> getSupporters() {
        return supporters;
    }

    public List<ContextComparison> getAnomalies() {
        return anomalies;
    }
}
