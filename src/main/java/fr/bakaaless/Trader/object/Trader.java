package fr.bakaaless.Trader.object;

import fr.bakaaless.Trader.plugin.TraderPlugin;
import fr.bakaaless.Trader.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trader implements Listener {

    private final TraderPlugin main;

    private final Map<Player, Inventory> playerInventoryMap;
    private final Map<Player, PlayerStatus> playerStatusMap;
    private final Map<Player, Integer> playerMoneyMap;
    private final Map<String, String> titles;

    int task;

    private TransactStatus status;

    public Trader(final Player ask, final Player answer) {
        this.main = TraderPlugin.getInstance();
        this.playerInventoryMap = new HashMap<>();
        this.playerStatusMap = new HashMap<>();
        this.playerMoneyMap = new HashMap<>();
        this.titles = new HashMap<>();
        this.status = TransactStatus.WAITING;
        this.init();
        this.main.getServer().getPluginManager().registerEvents(this, this.main);
        StringBuilder title1 = new StringBuilder(ask.getName());
        while (title1.length() < 14) {
            title1.append(" ");
        }
        title1.append("«»");
        while ((title1 + answer.getName()).length() < 30) {
            title1.append(" ");
        }
        title1.insert(0, "§d");
        title1 = new StringBuilder(title1.toString().replace("«»", "§8§l«»"));
        title1.append("§d").append(answer.getName());
        final Inventory inventory1 = this.main.getServer().createInventory(null, 9 * 6, title1.toString());
        this.draw(inventory1);
        this.playerInventoryMap.put(ask, inventory1);
        ask.openInventory(inventory1);

        StringBuilder title2 = new StringBuilder(answer.getName());
        while (title2.length() < 14) {
            title2.append(" ");
        }
        title2.append("«»");
        while ((title2 + ask.getName()).length() < 30) {
            title2.append(" ");
        }
        title2.insert(0, "§d");
        title2 = new StringBuilder(title2.toString().replace("«»", "§8§l«»"));
        title2.append("§d").append(ask.getName());
        final Inventory inventory2 = this.main.getServer().createInventory(null, 9 * 6, title2.toString());
        this.draw(inventory2);
        this.playerInventoryMap.put(answer, inventory2);
        answer.openInventory(inventory2);

        this.playerStatusMap.put(ask, PlayerStatus.WORKING);
        this.playerStatusMap.put(answer, PlayerStatus.WORKING);

        this.playerMoneyMap.put(ask, 0);
        this.playerMoneyMap.put(answer, 0);
    }


    private void init() {
        final String[] path = {"accept", "deny", "edit", "status.edit",
                "status.ready", "money.remove", "money.show", "money.add", "compteur"};
        for (final String message : path) {
            this.titles.put(message, this.main.getFileManager().getMessage("messages", "messages.info.menu." + message));
        }
    }

    private void cancel(final Player cancelled) {
        InventoryCloseEvent.getHandlerList().unregister(this);
        InventoryDragEvent.getHandlerList().unregister(this);
        InventoryClickEvent.getHandlerList().unregister(this);
        if (this.status.equals(TransactStatus.FINISH)) return;
        this.status = TransactStatus.CANCELLED;
        for (final Player player : this.playerInventoryMap.keySet()) {
            if (!player.equals(cancelled)) {
                player.sendMessage(this.main.getFileManager().getErrorWithPrefix("cancel.other"));
            } else
                cancelled.sendMessage(this.main.getFileManager().getErrorWithPrefix("cancel.player"));
            final Inventory inventory = player.getOpenInventory().getTopInventory();
            for (int slot = 0; slot < inventory.getSize(); slot++) {
                if (isInteractive(slot)) {
                    if (inventory.getItem(slot) == null) continue;
                    if (inventory.getItem(slot).getType().equals(Material.AIR)) continue;
                    addItem(player, inventory.getItem(slot));
                }
            }
            player.closeInventory();
        }
        this.status = TransactStatus.FINISH;
        this.main.removeTraders(this);
    }

    public void stop() {
        InventoryCloseEvent.getHandlerList().unregister(this);
        InventoryDragEvent.getHandlerList().unregister(this);
        InventoryClickEvent.getHandlerList().unregister(this);
        if (this.status.equals(TransactStatus.FINISH)) return;
        this.status = TransactStatus.CANCELLED;
        for (final Player player : this.playerInventoryMap.keySet()) {
            player.sendMessage(this.main.getFileManager().getErrorWithPrefix("cancel.console"));
            final Inventory inventory = player.getOpenInventory().getTopInventory();
            for (int slot = 0; slot < inventory.getSize(); slot++) {
                if (isInteractive(slot)) {
                    if (inventory.getItem(slot) == null) continue;
                    if (inventory.getItem(slot).getType().equals(Material.AIR)) continue;
                    addItem(player, inventory.getItem(slot));
                }
            }
            player.closeInventory();
        }
        this.status = TransactStatus.FINISH;
        this.main.removeTraders(this);
    }

    private void draw(final Inventory inventory) {
        ItemStack itemStack;
        ItemMeta itemMeta;
        for (int i = 0; i < 9 * 4; i++) {
            inventory.setItem(i, ItemUtils.get(Material.RED_STAINED_GLASS_PANE, "§7"));
        }
        for (int i = 9 * 4; i < 9 * 6; i++) {
            inventory.setItem(i, ItemUtils.get(Material.GRAY_STAINED_GLASS_PANE, "§7"));
        }
        for (int i = 0; i < 9 * 6; i++) {
            if (i != 4 && i != 13 && i != 22 && i != 31 && i != 49)
                continue;
            inventory.setItem(i, ItemUtils.get(Material.BLUE_STAINED_GLASS_PANE, "§7"));
        }
        for (int i = 0; i < 9 * 4; i++) {
            if ((i >= 4 && i <= 8) || (i >= 13 && i <= 17) || (i >= 22 && i <= 26) || i >= 31)
                continue;
            inventory.setItem(i, new ItemStack(Material.AIR));
        }
        inventory.setItem(36, ItemUtils.get(Material.RED_CONCRETE, this.titles.get("deny")));
        inventory.setItem(39, ItemUtils.get(Material.YELLOW_CONCRETE, this.titles.get("status.edit")));
        inventory.setItem(41, ItemUtils.get(Material.YELLOW_CONCRETE, this.titles.get("status.edit")));
        inventory.setItem(45, ItemUtils.get(Material.LIME_CONCRETE, this.titles.get("accept")));
        inventory.setItem(46, ItemUtils.skull(this.titles.get("money.remove"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ4YTk5ZGIyYzM3ZWM3MWQ3MTk5Y2Q1MjYzOTk4MWE3NTEzY2U5Y2NhOTYyNmEzOTM2Zjk2NWIxMzExOTMifX19"));
        inventory.setItem(47, ItemUtils.skull(this.titles.get("money.show").replace("%money%", "0"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmY3NWQxYjc4NWQxOGQ0N2IzZWE4ZjBhN2UwZmQ0YTFmYWU5ZTdkMzIzY2YzYjEzOGM4Yzc4Y2ZlMjRlZTU5In19fQ=="));
        inventory.setItem(48, ItemUtils.skull(this.titles.get("money.add"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2VkZDIwYmU5MzUyMDk0OWU2Y2U3ODlkYzRmNDNlZmFlYjI4YzcxN2VlNmJmY2JiZTAyNzgwMTQyZjcxNiJ9fX0="));
        inventory.setItem(51, ItemUtils.skull(this.titles.get("money.show").replace("%money%", "0"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmY3NWQxYjc4NWQxOGQ0N2IzZWE4ZjBhN2UwZmQ0YTFmYWU5ZTdkMzIzY2YzYjEzOGM4Yzc4Y2ZlMjRlZTU5In19fQ=="));
    }

    @EventHandler
    public void onInventoryDrag(final InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }
        final Player player = (Player) e.getWhoClicked();
        if (!this.playerInventoryMap.containsKey(player)) return;
        if (e.getInventory().equals(e.getView().getBottomInventory())) return;
        if (!this.playerInventoryMap.containsValue(e.getInventory())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }
        final Player player = (Player) e.getWhoClicked();
        if (!this.playerInventoryMap.containsKey(player)) return;
        if (!this.playerInventoryMap.containsValue(e.getView().getTopInventory())) return;
        if (e.getView().getBottomInventory().equals(e.getClickedInventory())) {
            if (!this.playerStatusMap.get(player).equals(PlayerStatus.WORKING)) {
                e.setCancelled(true);
            } else if (e.getClick().isShiftClick()) {
                e.setCancelled(true);
            }
            return;
        }
        final Integer slot = e.getSlot();
        if (slot < 0) return;
        if (isInteractive(e.getSlot())) {
            if (e.getClick().isShiftClick() || e.getClick().isCreativeAction() || e.getClick().isKeyboardClick()) {
                e.setCancelled(true);
                return;
            }
            if (!this.playerStatusMap.get(player).equals(PlayerStatus.WORKING)) {
                e.setCancelled(true);
                return;
            }
            if (!e.getClick().isLeftClick()) {
                e.setCancelled(true);
                return;
            }
            final Player other = this.playerInventoryMap.keySet().parallelStream().filter(player1 -> !player1.equals(player)).findFirst().get();
            ItemStack itemStack;
            if (e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
                itemStack = e.getCursor();
            } else {
                itemStack = ItemUtils.get(Material.RED_STAINED_GLASS_PANE, "§7");
            }
            this.playerInventoryMap.get(other).setItem(e.getSlot() + 5, itemStack);
        } else {
            e.setCancelled(true);
            if (e.getCurrentItem() == null) return;
            if (e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(this.titles.get("accept"))) {
                final Player other = this.playerInventoryMap.keySet().parallelStream().filter(player1 -> !player1.equals(player)).findFirst().get();
                this.playerInventoryMap.get(other).setItem(41, ItemUtils.get(Material.GREEN_CONCRETE, this.titles.get("status.ready")));
                e.getClickedInventory().setItem(39, ItemUtils.get(Material.GREEN_CONCRETE, this.titles.get("status.ready")));
                e.getClickedInventory().setItem(45, ItemUtils.get(Material.ORANGE_CONCRETE, this.titles.get("edit")));
                this.playerStatusMap.replace(player, PlayerStatus.WAITING);
                if (this.playerStatusMap.get(other).equals(PlayerStatus.WAITING) && this.status.equals(TransactStatus.WAITING)) {
                    this.progress();
                }
                return;
            }
            if (e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(this.titles.get("deny"))) {
                this.cancel(player);
                return;
            }
            if (e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(this.titles.get("edit"))) {
                final Player other = this.playerInventoryMap.keySet().parallelStream().filter(player1 -> !player1.equals(player)).findFirst().get();
                this.playerInventoryMap.get(other).setItem(41, ItemUtils.get(Material.YELLOW_CONCRETE, this.titles.get("status.edit")));
                e.getClickedInventory().setItem(39, ItemUtils.get(Material.YELLOW_CONCRETE, this.titles.get("status.edit")));
                e.getClickedInventory().setItem(45, ItemUtils.get(Material.LIME_CONCRETE, this.titles.get("accept")));
                this.playerStatusMap.replace(player, PlayerStatus.WORKING);
                return;
            }
            if (e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(this.titles.get("money.add"))) {
                final Player other = this.playerInventoryMap.keySet().parallelStream().filter(player1 -> !player1.equals(player)).findFirst().get();
                int money = this.playerMoneyMap.get(player);
                if (e.getClick().isLeftClick()) {
                    money++;
                } else if (e.getClick().isRightClick()) {
                    money += 50;
                } else if (e.getClick().isShiftClick()) {
                    money += 100;
                }
                final double balance = this.main.getEcon().getBalance(player);
                if (balance < money) {
                    money -= money - balance;
                }
                this.playerMoneyMap.replace(player, money);
                final ItemStack itemStack = this.playerInventoryMap.get(other).getItem(51);
                final ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(this.titles.get("money.show").replace("%money%", String.valueOf(money)));
                itemStack.setItemMeta(itemMeta);
                this.playerInventoryMap.get(other).setItem(51, itemStack);
                e.getClickedInventory().setItem(47, itemStack);
                return;
            }
            if (e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(this.titles.get("money.remove"))) {
                final Player other = this.playerInventoryMap.keySet().parallelStream().filter(player1 -> !player1.equals(player)).findFirst().get();
                int money = this.playerMoneyMap.get(player);
                if (money == 0) return;
                if (e.getClick().isLeftClick()) {
                    if (money < 1) money = 0;
                    else money--;
                } else if (e.getClick().isRightClick()) {
                    if (money < 50) money = 0;
                    else money -= 50;
                } else if (e.getClick().isShiftClick()) {
                    if (money < 100) money = 0;
                    else money -= 100;
                }
                this.playerMoneyMap.replace(player, money);
                final ItemStack itemStack = this.playerInventoryMap.get(other).getItem(51);
                final ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(this.titles.get("money.show").replace("%money%", String.valueOf(money)));
                itemStack.setItemMeta(itemMeta);
                this.playerInventoryMap.get(other).setItem(51, itemStack);
                e.getClickedInventory().setItem(47, itemStack);
                return;
            }
        }
    }

    @EventHandler
    public void onClose(final InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) {
            return;
        }
        if (this.status.equals(TransactStatus.FINISH)) return;
        final Player player = (Player) e.getPlayer();
        if (!this.playerInventoryMap.containsKey(player)) return;
        this.cancel(player);
    }

    private void progress() {
        this.status = TransactStatus.STARTING;
        final int[] time = {5 * 10};
        this.main.getServer().getScheduler().scheduleSyncRepeatingTask(this.main, () -> {
            if (this.status.equals(TransactStatus.STARTING)) {
                if (this.playerStatusMap.containsValue(PlayerStatus.WORKING)) {
                    for (final Inventory inventory : this.playerInventoryMap.values()) {
                        inventory.setItem(40, ItemUtils.get(Material.GRAY_STAINED_GLASS_PANE, "§7"));
                    }
                    this.status = TransactStatus.WAITING;
                    this.main.getServer().getScheduler().cancelTask(task);
                    return;
                }
                for (final Inventory inventory : this.playerInventoryMap.values()) {
                    inventory.setItem(40, ItemUtils.get(Material.REDSTONE_BLOCK, this.titles.get("compteur"), (int) Math.ceil(time[0] / 10d)));
                }
                if (time[0] == 0) {
                    this.status = TransactStatus.FINISH;
                    for (final Player player : this.playerInventoryMap.keySet()) {
                        final Inventory inventory = this.playerInventoryMap.get(player);
                        List<ItemStack> items = new ArrayList<>();
                        for (int slot = 0; slot < inventory.getSize(); slot++) {
                            if (isInteractive(slot)) {
                                if (inventory.getItem(slot) == null) continue;
                                if (inventory.getItem(slot).getType().equals(Material.AIR)) continue;
                                items.add(inventory.getItem(slot));
                            }
                        }
                        final Player other = this.playerInventoryMap.keySet().parallelStream().filter(player1 -> !player.equals(player1)).findFirst().get();
                        items.forEach(itemStack -> addItem(other, itemStack));
                        this.main.getEcon().depositPlayer(other, this.playerMoneyMap.get(player));
                        this.main.getEcon().withdrawPlayer(other, this.playerMoneyMap.get(other));
                        player.closeInventory();
                        player.sendMessage(this.main.getFileManager().getMessageWithPrefix("success"));
                    }
                    this.main.getServer().getScheduler().cancelTask(task);
                    return;
                }
                time[0]--;
            } else {
                this.main.getServer().getScheduler().cancelTask(task);
            }
        }, 0L, 2L);
    }

    private boolean isInteractive(final Integer slot) {
        return slot <= 3 || (slot >= 9 && slot <= 12) || (slot >= 18 && slot <= 21) || (slot >= 27 && slot <= 30);
    }

    private synchronized void addItem(final Player player, final ItemStack itemStack) {
        player.getLocation().getWorld().dropItem(player.getLocation(), itemStack).setPickupDelay(0);
    }

    private enum TransactStatus {
        WAITING,
        STARTING,
        CANCELLED,
        FINISH
    }

    private enum PlayerStatus {
        WORKING,
        WAITING,
        END
    }

    public Map<Player, Inventory> getPlayerInventoryMap() {
        return playerInventoryMap;
    }

    public Map<Player, PlayerStatus> getPlayerStatusMap() {
        return playerStatusMap;
    }

    public Map<Player, Integer> getPlayerMoneyMap() {
        return playerMoneyMap;
    }
}
