package net.starlegacy.explosionregen.nms;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class NMSUtils {
    private static NMS nms;
    private static Logger logger = Logger.getLogger(NMSUtils.class.getName());

    static {
        Map<String, Callable<NMS>> versionMap = new HashMap<>();
        versionMap.put("net.minecraft.server.v1_16_R2.WorldServer", NMS_v1_16_R2::new);

        for (String className : versionMap.keySet()) {
            try {
                Class.forName(className);
                nms = versionMap.get(className).call();
                logger.info("Loaded NMS adapter: " + className);
                break;
            } catch (Exception e) {
                // ignore
            }
        }

        if (nms == null) {
            logger.info("No NMS adapter found! Tile entities and entities won't regenerate properly.");
        }
    }

    @Nullable
    public static byte[] getTileEntity(Block block) {
        if (nms == null) {
            return null;
        }

        return nms.getTileEntity(block);
    }

    public static void setTileEntity(Block block, byte[] data) {
        if (nms == null) {
            return;
        }

        nms.setTileEntity(block, data);
    }

    @Nullable
    public static byte[] getEntityData(Entity entity) {
        if (nms == null) {
            return null;
        }

        return nms.getEntityData(entity);
    }

    public static void restoreEntityData(Entity entity, byte[] data) {
        if (nms == null) {
            return;
        }

        nms.restoreEntityData(entity, data);
    }
}
