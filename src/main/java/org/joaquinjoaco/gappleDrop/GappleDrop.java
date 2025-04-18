package org.joaquinjoaco.gappleDrop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class GappleDrop extends JavaPlugin implements Listener {

    private FileConfiguration config;
    private String prefix;
    private boolean pluginEnabled;
    private List<String> disabledWorlds;
    private int absorptionTime;
    private int absorptionLevel;
    private int regenTime;
    private int regenLevel;

    @Override
    public void onEnable() {
        // Save default config if not exists
        saveDefaultConfig();
        config = getConfig();

        // Load configuration
        loadConfigValues();

        // Register events
        getServer().getPluginManager().registerEvents(this, this);

        // Log enable message
        String enableMsg = config.getString("GappleDrop.messages.enable", "&2Plugin has been enabled!");
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + enableMsg));
    }

    @Override
    public void onDisable() {
        // Log disable message
        String disableMsg = config.getString("GappleDrop.messages.disable", "&aPlugin has been disabled!");
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + disableMsg));
    }

    private void loadConfigValues() {
        prefix = ChatColor.translateAlternateColorCodes('&', config.getString("GappleDrop.prefix", "&8[&6GappleDrop&8] "));
        pluginEnabled = config.getBoolean("GappleDrop.miscellaneous.enabled", true);
        disabledWorlds = config.getStringList("GappleDrop.disabled-worlds");
        absorptionTime = config.getInt("GappleDrop.gapple.absorption-time", 5) * 20; // Convert seconds to ticks
        absorptionLevel = config.getInt("GappleDrop.gapple.absorption-level", 1) - 1; // Levels are 0-based in API
        regenTime = config.getInt("GappleDrop.gapple.regen-time", 10) * 20; // Convert seconds to ticks
        regenLevel = config.getInt("GappleDrop.gapple.regen-level", 1) - 1; // Levels are 0-based in API
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!pluginEnabled) return;

        Player player = event.getEntity();
        World world = player.getWorld();

        // Check if the world is disabled
        if (disabledWorlds.contains(world.getName())) return;

        // Drop an enchanted golden apple
        ItemStack gapple = new ItemStack(Material.GOLDEN_APPLE, 1);
        world.dropItemNaturally(player.getLocation(), gapple);
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!pluginEnabled) return;

        // Check if the entity picking up the item is a player
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        ItemStack item = event.getItem().getItemStack();

        // Check if the picked-up item is an enchanted golden apple
        if (item.getType() == Material.GOLDEN_APPLE) {
            // Cancel the pickup event to prevent the item from going into the inventory
            event.setCancelled(true);

            // Remove the item from the world
            event.getItem().remove();

            // Apply potion effects
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, absorptionTime, absorptionLevel));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, regenTime, regenLevel));
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, regenTime, 0)); // Resistance I
        }
    }
}