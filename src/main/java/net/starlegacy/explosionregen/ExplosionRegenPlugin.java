package net.starlegacy.explosionregen;

import net.starlegacy.explosionregen.listener.EntityListener;
import net.starlegacy.explosionregen.listener.ExplosionListener;
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

public class ExplosionRegenPlugin extends JavaPlugin implements Listener {
    private Settings settings;
    private WorldData worldData;

    @Override
    public void onEnable() {
        loadConfigAndUpdateDefaults();
        initializeWorldData();
        registerEvents();
        scheduleRegen();
        registerCommand();
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

    private void registerCommand() {
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
        settings = new Settings(getConfig());
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
        long offset = Math.round((16 - distance) * getSettings().getDistanceDelay() * 1000);
        return now - offset; // subtract instead of add so it never happens after regenerating entities
    }
}
