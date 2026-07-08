package net.sakurain.mc.aeternumgenesis.item.set;

import net.sakurain.mc.aeternumgenesis.item.CustomItemTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Immutable data class representing an item set and its bonuses.
 */
public final class ItemSetTemplate {

    private final String id;
    private final String name;
    private final List<String> lore;
    private final List<String> itemIds;
    private final List<SetRequirement> requirements;
    private final List<SetBonus> bonuses;
    private final List<AdvancedBonus> advancedBonuses;

    public ItemSetTemplate(String id, String name, List<String> lore, List<String> itemIds,
                           List<SetRequirement> requirements, List<SetBonus> bonuses,
                           List<AdvancedBonus> advancedBonuses) {
        this.id = id;
        this.name = name;
        this.lore = lore == null ? List.of() : List.copyOf(lore);
        this.itemIds = itemIds == null ? List.of() : List.copyOf(itemIds);
        this.requirements = requirements == null ? List.of() : List.copyOf(requirements);
        this.bonuses = bonuses == null ? List.of() : List.copyOf(bonuses);
        this.advancedBonuses = advancedBonuses == null ? List.of() : List.copyOf(advancedBonuses);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public List<String> getItemIds() {
        return itemIds;
    }

    public List<SetRequirement> getRequirements() {
        return requirements;
    }

    public List<SetBonus> getBonuses() {
        return bonuses;
    }

    public List<AdvancedBonus> getAdvancedBonuses() {
        return advancedBonuses;
    }

    /**
     * Represents a bonus unlocked at a specific number of equipped set pieces.
     */
    public static final class SetBonus {

        private final int requiredPieces;
        private final List<CustomItemTemplate.EffectEntry> effects;
        private final List<String> messages;

        public SetBonus(int requiredPieces, List<CustomItemTemplate.EffectEntry> effects, List<String> messages) {
            this.requiredPieces = requiredPieces;
            this.effects = effects == null ? List.of() : List.copyOf(effects);
            this.messages = messages == null ? List.of() : List.copyOf(messages);
        }

        public int getRequiredPieces() {
            return requiredPieces;
        }

        public List<CustomItemTemplate.EffectEntry> getEffects() {
            return effects;
        }

        public List<String> getMessages() {
            return messages;
        }
    }

    /**
     * Represents a requirement that must be met for a set bonus to activate.
     */
    public static final class SetRequirement {

        private final String type;
        private final String value;
        private final int amount;

        public SetRequirement(String type, String value, int amount) {
            this.type = type;
            this.value = value;
            this.amount = amount;
        }

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }

        public int getAmount() {
            return amount;
        }
    }

    /**
     * Represents an advanced bonus with extra conditions or per-piece scaling.
     */
    public static final class AdvancedBonus {

        private final int minPieces;
        private final int maxPieces;
        private final String condition;
        private final List<CustomItemTemplate.EffectEntry> effects;
        private final List<String> messages;

        public AdvancedBonus(int minPieces, int maxPieces, String condition,
                             List<CustomItemTemplate.EffectEntry> effects, List<String> messages) {
            this.minPieces = minPieces;
            this.maxPieces = maxPieces;
            this.condition = condition;
            this.effects = effects == null ? List.of() : List.copyOf(effects);
            this.messages = messages == null ? List.of() : List.copyOf(messages);
        }

        public int getMinPieces() {
            return minPieces;
        }

        public int getMaxPieces() {
            return maxPieces;
        }

        public String getCondition() {
            return condition;
        }

        public List<CustomItemTemplate.EffectEntry> getEffects() {
            return effects;
        }

        public List<String> getMessages() {
            return messages;
        }
    }
}
