package net.starlegacy.explosionregen;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ExplosionRegenPlugin extends JavaPlugin {
    private Settings settings;
    private WorldData worldData;

    @Override
    public void onEnable() {
        loadConfigAndUpdateDefaults();
        worldData = new WorldData();

        Server server = getServer();

        server.getPluginManager().registerEvents(new ExplosionListener(this), this);

        BukkitScheduler scheduler = server.getScheduler();
        scheduler.runTaskTimer(this, () -> Regeneration.regenerate(this, false), 5L, 5L);
        scheduler.runTaskTimer(this, this::saveAll, 20L, (long) (20L * 60L * getSettings().getSaveInterval()));

        getCommand("regen").setExecutor((sender, command, label, args) -> {
            long start = System.nanoTime();
            int regenerated = Regeneration.regenerate(this, true);
            long elapsed = System.nanoTime() - start;

            String seconds = new BigDecimal(elapsed / 1_000_000_000.0)
                    .setScale(6, RoundingMode.HALF_UP)
                    .toPlainString();

            sender.sendMessage(ChatColor.GOLD + "Regenerated " + regenerated + " blocks in " + seconds + " seconds.");
            return true;
        });
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
        saveResource("config.yml", true);
        reloadConfig();
        settings.save(getConfig());
        saveConfig();
    }

    Settings getSettings() {
        return settings;
    }

    WorldData getWorldData() {
        return worldData;
    }
}
