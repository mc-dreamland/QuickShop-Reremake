package org.maxgamer.quickshop.gui;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.ModalForm;
import org.geysermc.floodgate.api.FloodgateApi;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.economy.AbstractEconomy;
import org.maxgamer.quickshop.api.shop.*;
import org.maxgamer.quickshop.shop.SimpleInfo;
import org.maxgamer.quickshop.util.$;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;

import java.util.List;
import java.util.Map;

public class ShopBuyGuiPocket {
    public static void sendBuyWindow(Player p, ShopManager manager) {
        ModalForm.Builder cwindow = ModalForm.builder();
        String s = QuickShop.getInstance().text().of(p, "integrations-check-failed-trade").forLocale();
        cwindow.title("确认")
                .content(s)
                .button1("确认")
                .button2("放弃")
                .validResultHandler(res -> {
                    if (res.clickedFirst()) {
                        manager.handleChat(p, "1");
                    } else {
                        manager.handleChat(p, "abort");
                    }
                });
        if (Util.isPocketPlayer(p)) {
            FloodgateApi.getInstance().sendForm(p.getUniqueId(), cwindow);
        }
    }

    public static void open(Player p, Shop shop) {
        CustomForm.Builder form = CustomForm.builder();
        form.title("玩家" + $.color(ChatColor.YELLOW, shop.ownerName()) + "的" + $.color(ChatColor.YELLOW, shop.getShopType().name()) + "商店");
//        ComplexForm form = new ComplexForm("玩家" + $.color(ChatColor.YELLOW, shop.getOwner()) + "的" + $.color(ChatColor.YELLOW, shop.getShopType().getLocalType()) + "商店");
        ItemStack item = shop.getItem();
        ItemMeta meta = item.getItemMeta();


        QuickShop plugin = QuickShop.getInstance();

        final AbstractEconomy eco = plugin.getEconomy();
        final double price = shop.getPrice();
        final Inventory playerInventory = p.getInventory();


        final double balance = eco.getBalance(shop.getOwner(), shop.getLocation().getWorld(), shop.getCurrency());

        form.label("" +
                $.color(ChatColor.GREEN, "物品名字： ") + (meta.hasDisplayName() ? meta.getDisplayName() + "(" + MsgUtil.getItemi18n(item.getType().name()) + ")" : MsgUtil.getItemi18n(item.getType().name())) + '\n' +
                $.color(ChatColor.GREEN, "单价： ") + $.color(ChatColor.YELLOW, String.valueOf(shop.getPrice())) + "\n" +
                $.color(ChatColor.YELLOW, "物品描述：") + "\n" +
                lore(meta.getLore()) +
                $.color(ChatColor.GREEN, "物品附魔：") + "\n" +
                enchantment(meta.getEnchants()) +
                "\n" +
                ChatColor.GREEN + "请输入你想" + (shop.getShopType() == ShopType.BUYING ? "§e出售§r" : "§e购买§r") + "的数量。\n" +
                ChatColor.GREEN + "您最多可 " + (shop.getShopType() == ShopType.BUYING ? "§e出售§r" : "§e购买§r")
                        + (shop.getShopType() == ShopType.BUYING ? getPlayerCanBuy(shop, balance, price, playerInventory) : getPlayerCanSell(shop, balance, price, playerInventory) ) + "个。\n" +
                ChatColor.GREEN + "然后点击提交");
        form.input("", "请输入数量");
        form.validResultHandler(res -> {

            Info info = new SimpleInfo(shop.getLocation(), ShopAction.BUY, null, null, shop, false);
            QuickShop.getInstance().getShopManager().getActions().put(p.getUniqueId(), info);
            p.chat(res.asInput(1));
        });
        if (Util.isPocketPlayer(p)) {
            FloodgateApi.getInstance().sendForm(p.getUniqueId(), form);
        }
    }

    private static String enchantment(Map<Enchantment, Integer> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        map.forEach((enchantment, lv) -> b.append("  ").append(MsgUtil.getEnchi18n(enchantment)).append(" ").append(lv).append('\n'));
        return b.toString();
    }

    private static String lore(List<String> lore) {
        if (lore == null || lore.isEmpty()) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        for (String line : lore) {
            b.append("  ").append(line).append('\n');
        }
        return b.toString();
    }


    private static int getPlayerCanBuy(Shop shop, double traderBalance, double price, Inventory playerInventory) {
        boolean isContainerCountingNeeded = shop.isUnlimited() && !shop.isAlwaysCountingContainer();
        if (shop.isFreeShop()) { // Free shop
            return isContainerCountingNeeded ? Util.countSpace(playerInventory, shop) : Math.min(shop.getRemainingStock(), Util.countSpace(playerInventory, shop));
        }
        int itemAmount = Math.min(Util.countSpace(playerInventory, shop), (int) Math.floor(traderBalance / price));
        if (!isContainerCountingNeeded) {
            itemAmount = Math.min(itemAmount, shop.getRemainingStock());
        }
        if (itemAmount < 0) {
            itemAmount = 0;
        }
        return itemAmount;
    }

    private static int getPlayerCanSell(Shop shop, double ownerBalance, double price, Inventory playerInventory) {
        boolean isContainerCountingNeeded = shop.isUnlimited() && !shop.isAlwaysCountingContainer();
        if (shop.isFreeShop()) {
            return isContainerCountingNeeded ? Util.countItems(playerInventory, shop) : Math.min(shop.getRemainingSpace(), Util.countItems(playerInventory, shop));
        }

        int items = Util.countItems(playerInventory, shop);
        final int ownerCanAfford = (int) (ownerBalance / price);
        if (!isContainerCountingNeeded) {
            // Amount check player amount and shop empty slot
            items = Math.min(items, shop.getRemainingSpace());
            // Amount check player selling item total cost and the shop owner's balance
            items = Math.min(items, ownerCanAfford);
        } else if (QuickShop.getInstance().getConfig().getBoolean("shop.pay-unlimited-shop-owners")) {
            // even if the shop is unlimited, the config option pay-unlimited-shop-owners is set to
            // true,
            // the unlimited shop owner should have enough money.
            items = Math.min(items, ownerCanAfford);
        }
        if (items < 0) {
            items = 0;
        }
        return items;
    }
}
