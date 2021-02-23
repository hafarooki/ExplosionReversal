package net.starlegacy.explosionreversal;

import net.starlegacy.explosionreversal.listener.EntityListener;
import net.starlegacy.explosionreversal.listener.ExplosionListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class ExplosionReversalPlugin extends JavaPlugin implements Listener {
    private Settings settings;
    private WorldData worldData;

    @Override
    public void onEnable() {
        loadConfigAndUpdateDefaults();
        initializeWorldData();
        registerEvents();
        scheduleRegen();
        registerCommands();
    }

    private void initializeWorldData() {
        worldData = new WorldData();
    }

    private void registerEvents() {
        Server server = getServer();
        server.getPluginManager().registerEvents(this, this);
        server.getPluginManager().registerEvents(new EntityListener(this), this);
        server.getPluginManager().registerEvents(new ExplosionListener(this), this);
    }

    private void scheduleRegen() {
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.runTaskTimer(this, () -> Regeneration.pulse(this), 5L, 5L);
    }

    private void registerCommands() {
        Objects.requireNonNull(getCommand("regen")).setExecutor((sender, command, label, args) -> {
            long start = System.nanoTime();
            int regeneratedBlocks = Regeneration.regenerateBlocks(this, true);
            int regeneratedEntities = Regeneration.regenerateEntities(this, true);
            long elapsed = System.nanoTime() - start;

            String seconds = new BigDecimal(elapsed / 1_000_000_000.0)
                    .setScale(6, RoundingMode.HALF_UP)
                    .toPlainString();

            sender.sendMessage(ChatColor.GOLD + "Regenerated " +
                    regeneratedBlocks + " blocks and " +
                    regeneratedEntities + " entities in " + seconds + " seconds.");
            return true;
        });

        Objects.requireNonNull(getCommand("explosionreversalreload")).setExecutor((sender, command, label, args) -> {
            this.settings = new Settings(getConfig());
            sender.sendMessage("Reloaded config. Please note that some changes may not take effect without restarting or reloading the server.");
            return true;
        });
    }

    @EventHandler
    public void onWorldSave(WorldSaveEvent event) {
        getWorldData().save(event.getWorld());
    }

    @Override
    public void onDisable() {
        saveAll();
    }

    private void saveAll() {
        Bukkit.getWorlds().forEach(worldData::save);
    }

    private void loadConfigAndUpdateDefaults() {
        saveDefaultConfig();
        this.settings = new Settings(getConfig());
    }

    public Settings getSettings() {
        return settings;
    }

    public WorldData getWorldData() {
        return worldData;
    }

    public long getExplodedTime(double explosionX, double explosionY, double explosionZ,
                                int blockX, int blockY, int blockZ) {
        long now = System.currentTimeMillis();
        double distance = Math.abs(explosionX - blockX) + Math.abs(explosionY - blockY) + Math.abs(explosionZ - blockZ);
        double distanceDelayMs = getSettings().getDistanceDelay() * 1000;
        double cap = getSettings().getDistanceDelayCap();
        long offset = Math.round(Math.min(cap, cap - distance) * distanceDelayMs);
        return now + offset;
    }
}
