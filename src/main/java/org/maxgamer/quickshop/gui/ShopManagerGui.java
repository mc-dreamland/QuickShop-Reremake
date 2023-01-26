package org.maxgamer.quickshop.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.quickshop.api.shop.Shop;
import redempt.redlib.inventorygui.InventoryGUI;

import java.util.Arrays;
import java.util.List;

public class ShopManagerGui {

    public static void open(Player player, Shop shop) {
        List<Integer> panel = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 48, 50, 52, 53);
        List<Integer> shopSlots = Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);
        InventoryGUI gui = new InventoryGUI(Bukkit.createInventory(null, 54, "商店列表"));

        for (Integer integer : panel) {
            gui.getInventory().setItem(integer, new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1));
        }

        ShopManagerGui.open(player, shop);

        gui.open(player);
    }

}
