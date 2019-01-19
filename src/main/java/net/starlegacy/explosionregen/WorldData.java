package net.starlegacy.explosionregen;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class WorldData {
    private Logger log = LoggerFactory.getLogger(getClass());

    private LoadingCache<World, List<ExplodedBlockData>> explodedBlocks = CacheBuilder.newBuilder()
            .weakKeys()
            .build(CacheLoader.from(this::load));

    private Type dataType = TypeToken.getParameterized(ArrayList.class, ExplodedBlockData.class).getType();

    private File getFile(World world) {
        return new File(world.getWorldFolder(), "data/explosionregen/explodedblocks.json.gz");
    }

    public List<ExplodedBlockData> get(World world) {
        return explodedBlocks.getUnchecked(world);
    }

    private List<ExplodedBlockData> load(World world) {
        File file = getFile(world);

        if (file.exists()) {
            try (Reader reader = new InputStreamReader(new GZIPInputStream(new FileInputStream(file)))) {
                return new Gson().fromJson(reader, dataType);
            } catch (IOException e) {
                log.error("Failed to load data for " + world.getName(), e);
                return new ArrayList<>();
            }
        } else {
            return new ArrayList<>();
        }
    }

    private void save(World world) {
        List<ExplodedBlockData> data = get(world);

        File file = getFile(world);
        file.getParentFile().mkdirs();

        try (Writer writer = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file)))) {
            new Gson().toJson(data, dataType, writer);
        } catch (IOException e) {
            log.error("Failed to save data for " + world.getName(), e);
        }
    }

    public void addAll(World world, Collection<ExplodedBlockData> explodedBlockData) {
        get(world).addAll(explodedBlockData);
    }
}
