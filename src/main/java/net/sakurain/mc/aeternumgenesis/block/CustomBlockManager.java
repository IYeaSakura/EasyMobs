package net.sakurain.mc.aeternumgenesis.block;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.item.CustomItemManager;
import net.sakurain.mc.aeternumgenesis.util.TemplateIdUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class CustomBlockManager {

    private final AeternumGenesisPlugin plugin;
    private final Map<String, CustomBlockTemplate> templates = new ConcurrentHashMap<>();
    private final Map<String, String> blockLocations = new ConcurrentHashMap<>();
    private final Map<String, UUID> holograms = new ConcurrentHashMap<>();
    private final File storageFile;

    public CustomBlockManager(@NotNull AeternumGenesisPlugin plugin) {
        this.plugin = plugin;
        this.storageFile = new File(plugin.getDataFolder(), "placed_blocks.yml");
        loadStorage();
    }

    public void loadConfigs(@NotNull Map<String, YamlConfiguration> configs) {
        templates.clear();
        for (Map.Entry<String, YamlConfiguration> entry : configs.entrySet()) {
            loadConfig(entry.getKey(), entry.getValue());
        }
    }

    private void loadConfig(@NotNull String fileName, @NotNull YamlConfiguration config) {
        for (String key : config.getKeys(false)) {
            if (!config.isConfigurationSection(key)) {
                continue;
            }
            String id = TemplateIdUtil.normalize(key);
            if (!TemplateIdUtil.isValid(id)) {
                plugin.getLogger().warning("Invalid block template id (must be lowercase [a-z0-9._-] and <= 64 chars): " + key);
                continue;
            }
            try {
                ConfigurationSection section = config.getConfigurationSection(key);
                if (section == null) {
                    continue;
                }
                CustomBlockTemplate template = new CustomBlockTemplate(plugin, id, section);
                templates.put(template.getId(), template);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to parse block '" + key + "' in " + fileName, e);
            }
        }
    }

    public int getTemplateCount() {
        return templates.size();
    }

    @Nullable
    public CustomBlockTemplate getTemplate(@NotNull String id) {
        return templates.get(id.toLowerCase());
    }

    public boolean hasTemplate(@NotNull String id) {
        return templates.containsKey(id.toLowerCase());
    }

    public boolean registerTemplate(@NotNull String id, @NotNull ConfigurationSection section) {
        String normalized = TemplateIdUtil.normalize(id);
        if (!TemplateIdUtil.isValid(normalized)) {
            plugin.getLogger().warning("Invalid block template id: " + id);
            return false;
        }
        try {
            CustomBlockTemplate template = new CustomBlockTemplate(plugin, normalized, section);
            templates.put(template.getId(), template);
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to register block template '" + id + "'", e);
            return false;
        }
    }

    public boolean unregisterTemplate(@NotNull String id) {
        return templates.remove(id.toLowerCase()) != null;
    }

    @NotNull
    public Set<String> getTemplateIds() {
        return Set.copyOf(templates.keySet());
    }

    @NotNull
    public Map<String, String> getBlockLocations() {
        return Map.copyOf(blockLocations);
    }

    @NotNull
    public String locationKey(@NotNull Location location) {
        World world = location.getWorld();
        if (world == null) {
            throw new IllegalArgumentException("Location has no world");
        }
        String worldName = world.getName().replace(";", "_").replace("\n", "_").replace("\r", "_");
        return worldName + ";" + location.getBlockX() + ";" + location.getBlockY() + ";" + location.getBlockZ();
    }

    @Nullable
    public Location parseLocation(@NotNull String key) {
        String[] parts = key.split(";");
        if (parts.length != 4) {
            return null;
        }
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) {
            return null;
        }
        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public boolean isCustomBlock(@NotNull Location location) {
        if (location.getWorld() == null) {
            return false;
        }
        return blockLocations.containsKey(locationKey(location));
    }

    @Nullable
    public String getBlockTemplateId(@NotNull Location location) {
        if (location.getWorld() == null) {
            return null;
        }
        return blockLocations.get(locationKey(location));
    }

    public boolean setCustomBlock(@NotNull Location location, @NotNull String templateId) {
        if (location.getWorld() == null) {
            return false;
        }
        CustomBlockTemplate template = getTemplate(templateId);
        if (template == null) {
            return false;
        }
        location.getBlock().setType(template.getMaterial(), false);
        blockLocations.put(locationKey(location), template.getId());
        spawnHologram(location, template);
        saveStorage();
        return true;
    }

    public boolean removeCustomBlock(@NotNull Location location) {
        if (location.getWorld() == null) {
            return false;
        }
        boolean removed = blockLocations.remove(locationKey(location)) != null;
        if (removed) {
            removeHologram(location);
            saveStorage();
        }
        return removed;
    }

    public void clear() {
        for (String key : new ArrayList<>(holograms.keySet())) {
            Location location = parseLocation(key);
            if (location != null) {
                removeHologram(location);
            }
        }
        blockLocations.clear();
    }

    public void loadStorage() {
        blockLocations.clear();
        if (!storageFile.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(storageFile);
        for (String key : config.getKeys(false)) {
            String templateId = config.getString(key);
            if (templateId != null) {
                blockLocations.put(key, templateId.toLowerCase());
            }
        }
    }

    public void saveStorage() {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<String, String> entry : blockLocations.entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }
        File temp = new File(storageFile.getParentFile(), storageFile.getName() + ".tmp");
        try {
            config.save(temp);
            if (storageFile.exists() && !storageFile.delete()) {
                plugin.getLogger().warning("Failed to remove old custom block storage");
            }
            if (!temp.renameTo(storageFile)) {
                plugin.getLogger().warning("Failed to rename custom block storage");
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save placed custom blocks", e);
        }
    }

    public void respawnAllHolograms() {
        removeInvalidHolograms();
        for (Map.Entry<String, String> entry : blockLocations.entrySet()) {
            Location location = parseLocation(entry.getKey());
            if (location == null || !location.getChunk().isLoaded()) {
                continue;
            }
            CustomBlockTemplate template = getTemplate(entry.getValue());
            if (template != null) {
                spawnHologram(location, template);
            }
        }
    }

    public void respawnHologramsInChunk(@NotNull Chunk chunk) {
        for (Map.Entry<String, String> entry : blockLocations.entrySet()) {
            Location location = parseLocation(entry.getKey());
            if (location == null || !chunk.equals(location.getChunk())) {
                continue;
            }
            CustomBlockTemplate template = getTemplate(entry.getValue());
            if (template != null) {
                spawnHologram(location, template);
            }
        }
    }

    private void removeInvalidHolograms() {
        for (Iterator<Map.Entry<String, UUID>> it = holograms.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, UUID> entry = it.next();
            Entity entity = Bukkit.getEntity(entry.getValue());
            if (entity == null || entity.isDead()) {
                it.remove();
            }
        }
    }

    public void spawnHologram(@NotNull Location location, @NotNull CustomBlockTemplate template) {
        if (!template.isShowNameTag()) {
            return;
        }
        removeHologram(location);
        Location spawnLoc = location.clone().add(0.5, 0.25, 0.5);
        ArmorStand stand = location.getWorld().spawn(spawnLoc, ArmorStand.class, s -> {
            s.setMarker(true);
            s.setInvisible(true);
            s.setInvulnerable(true);
            s.setGravity(false);
            s.setPersistent(false);
            s.setSmall(true);
            s.setAI(false);
            s.setCollidable(false);
            s.setCanPickupItems(false);
            s.customName(color(template.getDisplayName()));
            s.setCustomNameVisible(true);
        });
        holograms.put(locationKey(location), stand.getUniqueId());
    }

    public void removeHologram(@NotNull Location location) {
        UUID uuid = holograms.remove(locationKey(location));
        if (uuid == null) {
            return;
        }
        Entity entity = Bukkit.getEntity(uuid);
        if (entity != null) {
            entity.remove();
        }
    }

    @NotNull
    private Component color(@NotNull String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }

    public void dropCustomDrops(@NotNull Location location, @Nullable CustomBlockTemplate template) {
        if (template == null || !template.hasDrops() || location.getWorld() == null) {
            return;
        }
        java.util.concurrent.ThreadLocalRandom random = java.util.concurrent.ThreadLocalRandom.current();
        for (CustomBlockTemplate.DropEntry entry : template.getDrops()) {
            if (random.nextDouble() * 100.0 >= entry.chance()) {
                continue;
            }
            int amount = CustomItemManager.parseAmount(entry.amount());
            plugin.getItemAPI().dropItem(location, entry.itemId(), amount);
        }
    }
}
