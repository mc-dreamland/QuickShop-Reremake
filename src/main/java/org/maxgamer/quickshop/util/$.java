package org.maxgamer.quickshop.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.BiConsumer;

public class $ {

    public static <E> void walk(Collection<E> input, BiConsumer<E, Integer> consumer) {
        if (input == null || input.isEmpty() || consumer == null) {
            return;
        }
        Iterator<E> itr = input.iterator();
        int idx = 0;
        while (itr.hasNext()) {
            consumer.accept(itr.next(), idx++);
        }
    }

    public static Location loc(String world, double x, double y, double z) {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    public static String color(ChatColor color, String text) {
        return color + text + ChatColor.RESET;
    }

    public static boolean nil(Object any) {
        return any == null;
    }
}