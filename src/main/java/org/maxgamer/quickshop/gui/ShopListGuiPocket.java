package org.maxgamer.quickshop.gui;

import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.util.MsgUtil;

import java.util.ArrayList;
import java.util.List;

public class ShopListGuiPocket {


    public static void open(Player player) {

        List<Shop> playerShops = QuickShop.getInstance().getShopManager().getPlayerAllShops(player.getUniqueId());
        SimpleForm.Builder form = SimpleForm.builder();
        form.title("你的商店");
        form.content(String.format("你有 %d 个商店", playerShops.size()));
        List<Runnable> runs = new ArrayList<>();
        for (Shop playerShop : playerShops) {
            if (playerShop.getItem().hasItemMeta()) {
                if (playerShop.getItem().getItemMeta().hasDisplayName()) {
                    form.button(playerShop.getItem().getItemMeta().getDisplayName() + "§r\n 点击管理商店");
                }else {
                    form.button(MsgUtil.getItemi18n(playerShop.getItem().getType().name()) + "§r\n 点击管理商店");
                }
            } else {
                form.button(MsgUtil.getItemi18n(playerShop.getItem().getType().name()) + "§r\n 点击管理商店");
            }
            runs.add(() -> manager(player, playerShop));
        }

        form.validResultHandler((simpleForm, res) -> {
            runs.get(res.clickedButtonId()).run();
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);


    }

    private static void manager(Player player, Shop playerShop) {
        SimpleForm.Builder form = SimpleForm.builder();
        List<Runnable> runs = new ArrayList<>();
        form.title("管理商店");

        form.button("删除商店");
        runs.add(() -> {
            playerShop.delete();
            player.sendMessage("商店已删除");
        });

        form.button("传送至商店");
        runs.add(() -> {
            player.teleport(playerShop.getLocation());
            player.sendMessage("已传送至商店");
        });
        form.button("返回");
        runs.add(() -> open(player));

        form.validResultHandler((simpleForm, res) -> {
            runs.get(res.clickedButtonId()).run();
        });

        FloodgateApi.getInstance().sendForm(player.getUniqueId(), form);


    }
}
