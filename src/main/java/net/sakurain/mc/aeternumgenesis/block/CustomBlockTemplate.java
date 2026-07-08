package net.sakurain.mc.aeternumgenesis.block;

import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CustomBlockTemplate {

    private final AeternumGenesisPlugin plugin;
    private final String id;
    private final Material material;
    private final String displayName;
    private final boolean cancelPhysics;
    private final boolean cancelPiston;
    private final boolean showNameTag;
    private final List<String> allowedBreakItems;
    private final String requiredBreakPermission;
    private final String denyBreakMessage;
    private final List<DropEntry> drops;

    public CustomBlockTemplate(@NotNull AeternumGenesisPlugin plugin, @NotNull String id, @NotNull ConfigurationSection section) {
        this.plugin = plugin;
        this.id = id.toLowerCase();
        Material parsed = Material.matchMaterial(section.getString("material", "STONE"));
        this.material = parsed != null && parsed.isBlock() ? parsed : Material.STONE;
        this.displayName = section.getString("display_name", id);
        this.cancelPhysics = section.getBoolean("cancel_physics", true);
        this.cancelPiston = section.getBoolean("cancel_piston", true);
        this.showNameTag = section.getBoolean("show_name_tag", true);
        ConfigurationSection breakReq = section.getConfigurationSection("break_requirements");
        if (breakReq != null) {
            this.allowedBreakItems = breakReq.getStringList("allowed_items").stream().map(String::toLowerCase).toList();
            this.requiredBreakPermission = breakReq.getString("required_permission", "");
            this.denyBreakMessage = breakReq.getString("deny_message", "&cYou cannot break this block.");
        } else {
            this.allowedBreakItems = List.of();
            this.requiredBreakPermission = "";
            this.denyBreakMessage = "&cYou cannot break this block.";
        }
        this.drops = loadDrops(section.getConfigurationSection("drops"));
    }

    @NotNull
    private List<DropEntry> loadDrops(@Nullable ConfigurationSection section) {
        if (section == null) {
            return List.of();
        }
        List<DropEntry> result = new ArrayList<>();
        for (String key : section.getKeys(false)) {
            ConfigurationSection itemSection = section.getConfigurationSection(key);
            if (itemSection == null) {
                continue;
            }
            result.add(new DropEntry(
                    key,
                    itemSection.getString("amount", "1"),
                    itemSection.getDouble("chance", 100.0)
            ));
        }
        return result;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public Material getMaterial() {
        return material;
    }

    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    public boolean isCancelPhysics() {
        return cancelPhysics;
    }

    public boolean isCancelPiston() {
        return cancelPiston;
    }

    public boolean isShowNameTag() {
        return showNameTag;
    }

    public boolean canBreak(@Nullable Player player, @Nullable ItemStack tool) {
        if (!requiredBreakPermission.isEmpty() && (player == null || !player.hasPermission(requiredBreakPermission))) {
            return false;
        }
        if (allowedBreakItems.isEmpty()) {
            return true;
        }
        if (tool == null || tool.getType().isAir()) {
            return false;
        }
        String itemId = plugin != null ? plugin.getItemAPI().getItemTemplateId(tool) : null;
        String materialName = tool.getType().name().toLowerCase();
        for (String allowed : allowedBreakItems) {
            if (allowed.equalsIgnoreCase(materialName)) {
                return true;
            }
            if (allowed.startsWith("genesis:") && allowed.substring(8).equalsIgnoreCase(itemId)) {
                return true;
            }
            if (allowed.equalsIgnoreCase(itemId)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    public String getDenyBreakMessage() {
        return denyBreakMessage;
    }

    @NotNull
    public List<DropEntry> getDrops() {
        return Collections.unmodifiableList(drops);
    }

    public boolean hasDrops() {
        return !drops.isEmpty();
    }

    public record DropEntry(@NotNull String itemId, @NotNull String amount, double chance) {
    }
}
