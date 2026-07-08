package net.sakurain.mc.aeternumgenesis.ai;

import java.util.*;

class AggroTable {

    private final Map<UUID, Double> threats = new LinkedHashMap<>();
    private final int maxSize;

    AggroTable(int maxSize) {
        this.maxSize = maxSize;
    }

    void addThreat(UUID target, double damage) {
        threats.merge(target, damage, Double::sum);
        if (threats.size() > maxSize) {
            Iterator<Map.Entry<UUID, Double>> it = threats.entrySet().iterator();
            it.next();
            it.remove();
        }
    }

    double getThreat(UUID target) {
        return threats.getOrDefault(target, 0.0);
    }

    void decay(double factor) {
        threats.replaceAll((k, v) -> v * factor);
        threats.values().removeIf(v -> v < 0.1);
    }
}
