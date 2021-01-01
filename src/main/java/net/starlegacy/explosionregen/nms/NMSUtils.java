package net.starlegacy.explosionregen.nms;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class NMSUtils {
    private static NMSWrapper nmsWrapper;
    private static Logger logger = Logger.getLogger(NMSUtils.class.getName());

    static {
        Map<String, Callable<NMSWrapper>> versionMap = new HashMap<>();
        versionMap.put("net.minecraft.server.v1_16_R3.WorldServer", NMSWrapper_v1_16_R3::new);

        for (String className : versionMap.keySet()) {
            try {
                Class.forName(className);
                nmsWrapper = versionMap.get(className).call();
                logger.info("Loaded NMS adapter: " + className);
                break;
            } catch (Exception e) {
                // ignore
            }
        }

        if (nmsWrapper == null) {
            logger.info("No NMS adapter found! Tile entities and entities won't regenerate properly.");
        }
    }

    @Nullable
    public static byte[] getTileEntity(Block block) {
        if (nmsWrapper == null) {
            return null;
        }

        return nmsWrapper.getTileEntity(block);
    }

    public static void setTileEntity(Block block, byte[] data) {
        if (nmsWrapper == null) {
            return;
        }

        nmsWrapper.setTileEntity(block, data);
    }

    @Nullable
    public static byte[] getEntityData(Entity entity) {
        if (nmsWrapper == null) {
            return null;
        }

        return nmsWrapper.getEntityData(entity);
    }

    public static void restoreEntityData(Entity entity, byte[] data) {
        if (nmsWrapper == null) {
            return;
        }

        nmsWrapper.restoreEntityData(entity, data);
    }
}
