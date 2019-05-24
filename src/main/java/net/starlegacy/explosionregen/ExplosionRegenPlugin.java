package net.starlegacy.explosionregen;

import org.bukkit.plugin.java.JavaPlugin;

public class ExplosionRegenPlugin extends JavaPlugin {
    private Settings settings;
    private WorldData worldData;

    @Override
    public void onEnable() {
        loadConfigAndUpdateDefaults();
        worldData = new WorldData();
        getServer().getPluginManager().registerEvents(new ExplosionListener(this), this);
        getServer().getScheduler().runTaskTimer(this, () -> Regeneration.regenerate(this), 5L, 5L);
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
