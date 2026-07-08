package net.sakurain.mc.aeternumgenesis.listener;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.sakurain.mc.aeternumgenesis.AeternumGenesisPlugin;
import net.sakurain.mc.aeternumgenesis.api.event.CustomBlockBreakEvent;
import net.sakurain.mc.aeternumgenesis.api.event.CustomBlockPlaceEvent;
import net.sakurain.mc.aeternumgenesis.block.CustomBlockManager;
import net.sakurain.mc.aeternumgenesis.block.CustomBlockTemplate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomBlockListener implements Listener {

    private final AeternumGenesisPlugin plugin;

    public CustomBlockListener(@NotNull AeternumGenesisPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        String itemTemplateId = plugin.getItemAPI().getItemTemplateId(item);
        if (itemTemplateId == null) {
            return;
        }
        String blockId = resolveBlockId(itemTemplateId);
        if (blockId == null) {
            return;
        }
        CustomBlockManager manager = plugin.getBlockManager();
        if (!manager.hasTemplate(blockId)) {
            return;
        }
        Location location = event.getBlock().getLocation();
        CustomBlockPlaceEvent placeEvent = new CustomBlockPlaceEvent(location, blockId, player);
        Bukkit.getPluginManager().callEvent(placeEvent);
        if (placeEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }
        manager.setCustomBlock(location, blockId);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        handleBreak(event.getBlock(), event.getPlayer(), event);
    }

    private void handleBreak(@NotNull Block block, @Nullable Player player, @NotNull org.bukkit.event.Cancellable cancellable) {
        Location location = block.getLocation();
        CustomBlockManager manager = plugin.getBlockManager();
        String templateId = manager.getBlockTemplateId(location);
        if (templateId == null) {
            return;
        }
        CustomBlockTemplate template = manager.getTemplate(templateId);
        if (template != null && player != null && !template.canBreak(player, player.getInventory().getItemInMainHand())) {
            cancellable.setCancelled(true);
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(template.getDenyBreakMessage()));
            return;
        }
        CustomBlockBreakEvent breakEvent = new CustomBlockBreakEvent(location, templateId, player);
        Bukkit.getPluginManager().callEvent(breakEvent);
        if (breakEvent.isCancelled()) {
            cancellable.setCancelled(true);
            return;
        }
        cancellable.setCancelled(true);
        block.setType(org.bukkit.Material.AIR, true);
        manager.removeCustomBlock(location);
        if (breakEvent.isDropItems() && template != null) {
            manager.dropCustomDrops(location.add(0.5, 0.5, 0.5), template);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (plugin.getBlockManager().isCustomBlock(block.getLocation())) {
                CustomBlockTemplate template = plugin.getBlockManager().getTemplate(plugin.getBlockManager().getBlockTemplateId(block.getLocation()));
                if (template == null || template.isCancelPiston()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (plugin.getBlockManager().isCustomBlock(block.getLocation())) {
                CustomBlockTemplate template = plugin.getBlockManager().getTemplate(plugin.getBlockManager().getBlockTemplateId(block.getLocation()));
                if (template == null || template.isCancelPiston()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Location location = event.getBlock().getLocation();
        CustomBlockManager manager = plugin.getBlockManager();
        String templateId = manager.getBlockTemplateId(location);
        if (templateId == null) {
            return;
        }
        CustomBlockTemplate template = manager.getTemplate(templateId);
        if (template != null && template.isCancelPhysics()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        plugin.getBlockManager().respawnHologramsInChunk(event.getChunk());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        CustomBlockManager manager = plugin.getBlockManager();
        event.blockList().removeIf(block -> {
            String templateId = manager.getBlockTemplateId(block.getLocation());
            if (templateId == null) {
                return false;
            }
            CustomBlockTemplate template = manager.getTemplate(templateId);
            if (template != null) {
                manager.removeCustomBlock(block.getLocation());
                if (template.hasDrops()) {
                    manager.dropCustomDrops(block.getLocation().add(0.5, 0.5, 0.5), template);
                }
            }
            return true;
        });
    }

    @Nullable
    private String resolveBlockId(@NotNull String itemTemplateId) {
        net.sakurain.mc.aeternumgenesis.item.CustomItemTemplate itemTemplate = plugin.getItemManager().getTemplate(itemTemplateId);
        if (itemTemplate == null) {
            return null;
        }
        String blockId = itemTemplate.getBlockId();
        if (blockId != null && plugin.getBlockManager().hasTemplate(blockId)) {
            return blockId;
        }
        return null;
    }
}
