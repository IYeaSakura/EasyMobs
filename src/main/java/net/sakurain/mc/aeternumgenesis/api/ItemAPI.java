package net.sakurain.mc.aeternumgenesis.api;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

public interface ItemAPI {

    boolean isCustomItem(@Nullable ItemStack item);

    @Nullable
    String getItemTemplateId(@Nullable ItemStack item);

    boolean hasTemplate(@NotNull String templateId);

    @NotNull
    Optional<ItemStack> buildItem(@NotNull String templateId);

    @NotNull
    Optional<ItemStack> buildItem(@NotNull String templateId, int amount);

    boolean giveItem(@NotNull Player player, @NotNull String templateId);

    boolean giveItem(@NotNull Player player, @NotNull String templateId, int amount);

    @NotNull
    Optional<ItemStack> dropItem(@NotNull Location location, @NotNull String templateId, int amount);

    @NotNull
    Collection<String> getAllTemplateIds();

    @NotNull
    Optional<String> getDisplayName(@NotNull String templateId);

    @NotNull
    NamespacedKey getItemIdKey();

    /**
     * Registers or overrides an item template from a configuration section at runtime.
     *
     * @param templateId the template id
     * @param config     the configuration section
     * @return true if registered successfully
     */
    boolean registerTemplate(@NotNull String templateId, @NotNull ConfigurationSection config);

    /**
     * Unregisters a runtime-registered item template.
     *
     * @param templateId the template id
     * @return true if removed
     */
    boolean unregisterTemplate(@NotNull String templateId);
}
