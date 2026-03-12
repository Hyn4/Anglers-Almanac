package dev.rm20.anglersalmanac.AlmanacBook;

import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.protocol.ItemBase;
import com.hypixel.hytale.protocol.ItemTranslationProperties;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateItems;
import com.hypixel.hytale.protocol.packets.assets.UpdateTranslations;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

//TODO: what ever I was doing here.
public class AlmanacBook {

    public static void syncCustomBookDisplay(PlayerRef playerRef, String playerUuid, String playerName) {
        Item baseItem = Item.getAssetMap().getAsset("Almanac_Book");
        String customId = "almanac.book." + playerUuid;
        Item customItem = CloneItem(customId, baseItem);
        registerItemOnServer(customId, baseItem);

        ItemBase definition = customItem.toPacket().clone();

        definition.id = customId;
        definition.translationProperties = new ItemTranslationProperties(
                customId+".name",
                customId+".description"
        );

        Map<String, String> translations = new HashMap<>();
        translations.put(customId+".name", playerName + "'s Angler's Almanac");
        translations.put(customId+".description", "<color is=\"#AAAAAA\">Bound to ID:</color>\n<i>" + playerUuid + "</i>");

        UpdateTranslations packet = new UpdateTranslations();
        packet.type = UpdateType.AddOrUpdate;
        packet.translations = translations;
        playerRef.getPacketHandler().writeNoCache(packet);

        UpdateItems itemPacket = new UpdateItems();
        itemPacket.type = UpdateType.AddOrUpdate;
        itemPacket.items = new HashMap<>();
        itemPacket.items.put(customId, definition);
        playerRef.getPacketHandler().writeNoCache(itemPacket);
    }

    public static Item CloneItem(String newId, Item original) {
        Item newItem = new Item(original);

        try {
            java.lang.reflect.Field idField = Item.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(newItem, newId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return newItem;
    }

    public static void registerItemOnServer(String customId, Item baseItem) {
        try {
            AssetMap<String, Item> assetMap = Item.getAssetMap();
            Field mapField = assetMap.getClass().getDeclaredField("assetMap");
            Field lockField = assetMap.getClass().getDeclaredField("assetMapLock");

            mapField.setAccessible(true);
            lockField.setAccessible(true);
            Map<String, Item> internalMap = (Map<String, Item>) mapField.get(assetMap);
            StampedLock lock = (StampedLock) lockField.get(assetMap);
            if (internalMap.containsKey(customId)) return;

            // Thread-safe injection
            long stamp = lock.writeLock();
            try {
                internalMap.put(customId, baseItem);
            } finally {
                lock.unlockWrite(stamp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
