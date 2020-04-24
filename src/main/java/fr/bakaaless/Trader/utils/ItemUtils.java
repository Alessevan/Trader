package fr.bakaaless.Trader.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;

public class ItemUtils {

    public static ItemStack get(final Material material, final String displayName) {
        return get(material, displayName, 1);
    }

    public static ItemStack get(final Material material, final String displayName, final int amount) {
        final ItemStack itemStack = new ItemStack(material, amount);
        final ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(displayName);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack skull(final String displayName, String texture) {
        if (!texture.contains("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv")) {
            texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" + texture;
        }
        final ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD, 1);
        final SkullMeta itemMeta = (SkullMeta) itemStack.getItemMeta();
        final GameProfile gameProfile = new GameProfile(UUID.randomUUID(), null);
        gameProfile.getProperties().put("textures", new Property("textures", texture));
        try {
            assert itemMeta != null;
            final Field declaredField = itemMeta.getClass().getDeclaredField("profile");
            declaredField.setAccessible(true);
            declaredField.set(itemMeta, gameProfile);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
        itemMeta.setDisplayName(displayName);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
