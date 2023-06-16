/*
 * This file is a part of project QuickShop, the name is ShopPurger.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.shop;

import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.economy.EconomyTransaction;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.util.PlayerFinder;
import org.maxgamer.quickshop.util.Util;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;


public class ShopPurger {
    private final QuickShop plugin;
    private volatile boolean executing;

    public ShopPurger(QuickShop plugin) {
        this.plugin = plugin;
    }

    public void purge() {
        if (!plugin.getConfig().getBoolean("purge.enabled")) {
            plugin.getLogger().info("[Shop Purger] Purge not enabled!");
            return;
        }
        if (executing) {
            plugin.getLogger().info("[Shop Purger] Another purge task still running!");
        } else {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::run);
        }
    }

    private void run() {
        Util.ensureThread(true);
        executing = true;
        try {
            if (plugin.getConfig().getBoolean("purge.backup")) {
                String backupFileName = "shop-purge-backup-" + UUID.randomUUID() + ".txt";
                Util.makeExportBackup(backupFileName);
                plugin.getLogger().info("[Shop Purger] We have backup shop data as " + backupFileName + ", if you ran into any trouble, please rename it to recovery.txt then use /qs recovery in console to rollback!");
            }
            plugin.getLogger().info("[Shop Purger] Scanning and removing shops....");
            List<Shop> pendingRemovalShops = new ArrayList<>();
            int days = plugin.getConfig().getInt("purge.days", 360);
            boolean deleteBanned = plugin.getConfig().getBoolean("purge.banned");
            boolean skipOp = plugin.getConfig().getBoolean("purge.skip-op");
            boolean returnCreationFee = plugin.getConfig().getBoolean("purge.return-create-fee");
            for (Shop shop : plugin.getShopManager().getAllShops()) {
                OfflinePlayer player = PlayerFinder.findOfflinePlayerByUUID(shop.getOwner());
                if (!player.hasPlayedBefore()) {
                    Util.debugLog("Shop " + shop + " detection skipped: Owner never played before.");
                    continue;
                }
                long lastPlayed = player.getLastPlayed();
                if (lastPlayed == 0) {
                    continue;
                }
                if (player.isOnline()) {
                    continue;
                }
                if (player.isOp() && skipOp) {
                    continue;
                }
                boolean markDeletion = player.isBanned() && deleteBanned;
                //noinspection ConstantConditions
                long noOfDaysBetween = ChronoUnit.DAYS.between(Util.getDateTimeFromTimestamp(lastPlayed), Util.getDateTimeFromTimestamp(System.currentTimeMillis()));
                if (noOfDaysBetween > days) {
                    markDeletion = true;
                }
                if (!markDeletion) {
                    continue;
                }
                pendingRemovalShops.add(shop);
            }
            if (pendingRemovalShops.size() > 0) {
                plugin.getLogger().info("[Shop Purger] Found " + pendingRemovalShops.size() + " need to removed, will remove in the next tick.");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            Iterator<Shop> shopIterator = pendingRemovalShops.iterator();
                            long startTimeMs = System.currentTimeMillis();
                            while (shopIterator.hasNext()) {
                                if (startTimeMs - System.currentTimeMillis() > 130) {
                                    plugin.getLogger().info("[Shop Purger] it seems taking long time to purge shop (>130ms), do it in the next tick...");
                                }
                                Shop shop = shopIterator.next();
                                shop.delete(false);
                                if (returnCreationFee) {
                                    EconomyTransaction transaction =
                                            EconomyTransaction.builder()
                                                    .amount(plugin.getConfig().getDouble("shop.cost"))
                                                    .allowLoan(false)
                                                    .core(QuickShop.getInstance().getEconomy())
                                                    .currency(shop.getCurrency())
                                                    .world(shop.getLocation().getWorld())
                                                    .to(shop.getOwner())
                                                    .build();
                                    transaction.failSafeCommit();
                                }
                                shopIterator.remove();
                                plugin.getLogger().info("[Shop Purger] Shop " + shop + " has been purged.");
                            }
                            plugin.getLogger().info("[Shop Purger] Task completed, " + pendingRemovalShops.size() + " shops was purged");
                        } finally {
                            this.cancel();
                            executing = false;
                        }
                    }
                }.runTaskTimer(plugin, 1L, 1L);
            } else {
                plugin.getLogger().info("[Shop Purger] Task completed, No shops need to purge.");
                executing = false;
            }
        } finally {
            executing = false;
        }
    }
}
