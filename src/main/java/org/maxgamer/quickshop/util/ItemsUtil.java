package org.maxgamer.quickshop.util;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ItemsUtil {

    public static ItemStack changeItemOwner(UUID uuid, ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
            return null;
        }
        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            return itemStack;
        }
        NBTItem nbtItem = new NBTItem(itemStack);
        if (!nbtItem.hasTag("ItemInfo")) {
            return itemStack;
        } else {
            NBTCompound itemInfo = nbtItem.getOrCreateCompound("ItemInfo");
            itemInfo.setString("Owner", player.getName());
            itemInfo.setString("OwnerUUID", player.getUniqueId().toString());
            return nbtItem.getItem();
        }
    }


    public static boolean isItemOwner(UUID uuid, ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
            return true;
        }
        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            return false;
        }
        NBTItem nbtItem = new NBTItem(itemStack);
        if (!nbtItem.hasTag("ItemInfo")) {
            return true;
        } else {
            NBTCompound itemInfo = nbtItem.getOrCreateCompound("ItemInfo");
            if (!itemInfo.hasTag("OwnerUUID")) {
                return true;
            }
            return itemInfo.getString("OwnerUUID").equals(player.getUniqueId().toString());
        }
    }

    public static boolean isSameNbt(ItemStack requireStack, ItemStack givenStack) {
        NBTItem requireNbt = new NBTItem(requireStack);
        NBTItem givenNbt = new NBTItem(givenStack);
        if (requireNbt.hasNBTData() && givenNbt.hasNBTData()) {
            NBTCompound itemInfoR = requireNbt.getCompound("ItemInfo");
            NBTCompound itemInfoG = givenNbt.getCompound("ItemInfo");
            itemInfoR.removeKey("Owner");
            itemInfoR.removeKey("OwnerUUID");
            itemInfoR.removeKey("Creator");
            itemInfoR.removeKey("CreatorUUID");
            itemInfoR.removeKey("RandomId");
            itemInfoG.removeKey("Owner");
            itemInfoG.removeKey("OwnerUUID");
            itemInfoG.removeKey("Creator");
            itemInfoG.removeKey("CreatorUUID");
            itemInfoG.removeKey("RandomId");
            boolean equals = itemInfoR.equals(itemInfoG);
            return equals;
        }

        return false;


    }

    public static boolean itemShouldCheck(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
            return false;
        }

        NBTItem nbtItem = new NBTItem(itemStack);
        if (!nbtItem.hasTag("ItemInfo")) {
            return false;
        } else {
            NBTCompound itemInfo = nbtItem.getOrCreateCompound("ItemInfo");
            return itemInfo.hasTag("CreatorUUID") && itemInfo.hasTag("OwnerUUID");
        }
    }

}
