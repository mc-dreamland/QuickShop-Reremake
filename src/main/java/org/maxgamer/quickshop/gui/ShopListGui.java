package org.maxgamer.quickshop.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.shop.Shop;
import redempt.redlib.inventorygui.InventoryGUI;
import redempt.redlib.inventorygui.ItemButton;
import redempt.redlib.itemutils.ItemBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShopListGui {


    public static void open(Player player, int page) {
        List<Integer> panel = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 48, 50, 52, 53);
        List<Integer> shopSlots = Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);
        InventoryGUI gui = new InventoryGUI(Bukkit.createInventory(null, 54, "商店列表"));
        List<Shop> playerShops = QuickShop.getInstance().getShopManager().getPlayerAllShops(player.getUniqueId());

        for (Integer integer : panel) {
            gui.getInventory().setItem(integer, new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1, (short) 15));
        }

        if (page > 0) {
            ItemStack p = new ItemStack(Material.ARROW, 1);
            ItemMeta pItemMeta = p.getItemMeta();
            pItemMeta.setDisplayName("§f上一页");
            p.setItemMeta(pItemMeta);

            ItemButton button = ItemButton.create(new ItemBuilder(p), e -> {
                gui.setDestroyOnClose(true);
                open(player, page - 1);
            });
            gui.addButton(button, 47);
        }

        if (playerShops.size() - 28 * page > 28) {
            ItemStack p = new ItemStack(Material.ARROW, 1);
            ItemMeta pItemMeta = p.getItemMeta();
            pItemMeta.setDisplayName("§f下一页");
            p.setItemMeta(pItemMeta);

            ItemButton button = ItemButton.create(new ItemBuilder(p), e -> {
                gui.setDestroyOnClose(true);
                open(player, page + 1);
            });
            gui.addButton(button, 51);
        }

        int size = Math.min(playerShops.size() - 28 * page, 28);

        for (int i = 0; i < size; i++) {
            Shop shop = playerShops.get(i + 28 * page);
            ItemStack item = shop.getItem().clone();
            ItemMeta itemMeta = item.getItemMeta();
            List<String> lore = itemMeta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.add("");
            lore.add("§f世界: §e" + shop.getLocation().getWorld().getName());
            lore.add("§f坐标: §eX:" + shop.getLocation().getBlockX() + " Y:" + shop.getLocation().getBlockY() + " Z:" + shop.getLocation().getBlockZ());
            lore.add("§f类型: §e" + shop.getShopType());
            lore.add("§f单价: §e" + shop.getPrice());
            lore.add("");
            lore.add("§e左键 §f传送至商店");
            lore.add("§e右键 §f删除商店");
            itemMeta.setLore(lore);
            item.setItemMeta(itemMeta);


            ItemButton button = ItemButton.create(new ItemBuilder(item), e -> {
                gui.setDestroyOnClose(true);
                ClickType click = e.getClick();
                if (click.isLeftClick()) {
                    e.getWhoClicked().teleport(shop.getLocation());
                    e.getWhoClicked().closeInventory();
                } else if (click.isRightClick()) {
                    openDeleteConfirm(player, shop);
                }
            });
            gui.addButton(button, shopSlots.get(i));
        }

        gui.open(player);
    }

    private static void openDeleteConfirm(Player player, Shop shop) {

        InventoryGUI gui = new InventoryGUI(Bukkit.createInventory(null, 27, "快捷商店 - 操作确认"));

        ItemStack cancel = new ItemStack(Material.LIME_WOOL, 1);
        ItemMeta cancel_itemMeta = cancel.getItemMeta();
        cancel_itemMeta.setDisplayName("§c我再考虑一下");
        cancel_itemMeta.setLore(Arrays.asList("§r", "§e点击 §f返回上一页"));
        cancel.setItemMeta(cancel_itemMeta);
        ItemStack confirm = new ItemStack(Material.RED_WOOL, 1, (short) 5);
        ItemMeta confirm_itemMeta = confirm.getItemMeta();
        confirm_itemMeta.setDisplayName("§a确认删除");
        confirm_itemMeta.setLore(Arrays.asList("§r", "§c警告！当前操作无法撤销！", "§r", "§e点击 §f移除当前商店"));
        confirm.setItemMeta(confirm_itemMeta);


        ItemButton cancel_button = ItemButton.create(new ItemBuilder(cancel), e -> {
            gui.setDestroyOnClose(true);
            open(player, 0);
        });
        ItemButton confirm_button = ItemButton.create(new ItemBuilder(confirm), e -> {
            gui.setDestroyOnClose(true);
            shop.delete();
            open(player, 0);
        });
        gui.addButton(cancel_button, 11);
        gui.addButton(confirm_button, 15);

        gui.open(player);
    }


}
