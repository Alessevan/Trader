package fr.bakaaless.Trader.commands;

import fr.bakaaless.Trader.object.Trader;
import fr.bakaaless.Trader.plugin.TraderPlugin;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Executor implements CommandExecutor, TabCompleter {

    private final TraderPlugin main;

    private final Map<Player, Player> accept;

    public Executor() {
        this.main = TraderPlugin.getInstance();
        this.accept = new HashMap<>();
        this.main.getCommand("echange").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        final Player player = (Player) sender;
        try {
            final int radius = this.main.getFileManager().getFile("config").getInt("radius");
            if (args[0].equalsIgnoreCase("accept")) {
                if (!this.accept.containsKey(player)) {
                    player.sendMessage(this.main.getFileManager().getErrorWithPrefix("none"));
                    return true;
                }
                if (!this.getPlayerRadius(player.getLocation(), radius).contains(this.accept.get(player))) {
                    player.sendMessage(this.main.getFileManager().getErrorWithPrefix("outrange"));
                    return true;
                }
                player.sendMessage(this.main.getFileManager().getMessageWithPrefix("accept"));
                this.accept.get(player).sendMessage(this.main.getFileManager().getMessageWithPrefix("accept"));
                this.main.addTraders(new Trader(player, this.accept.get(player)));
                this.accept.remove(player);
                return true;
            } else if (args[0].equalsIgnoreCase("deny")) {
                if (!this.accept.containsKey(player)) {
                    player.sendMessage(this.main.getFileManager().getErrorWithPrefix("none"));
                    return true;
                }
                player.sendMessage(this.main.getFileManager().getMessageWithPrefix("deny"));
                this.accept.get(player).sendMessage(this.main.getFileManager().getErrorWithPrefix("deny"));
                this.accept.remove(player);
                return true;
            } else if (args[0].equalsIgnoreCase("stop")) {
                if (player.hasPermission("trader.stop")) {
                    this.main.stopAll();
                    player.sendMessage(this.main.getFileManager().getMessageWithPrefix("stop"));
                    return true;
                }
            }
            for (final Player players : this.getPlayerRadius(player.getLocation(), radius)) {
                if (!players.getName().toLowerCase().equalsIgnoreCase(args[0])) continue;
                if (player.equals(players)) {
                    player.sendMessage(this.main.getFileManager().getErrorWithPrefix("yourself"));
                    return true;
                }
                if (this.accept.containsKey(player) && this.accept.get(player).equals(players)) {
                    return player.performCommand(label + " accept");
                }
                player.sendMessage(this.main.getFileManager().getMessageWithPrefix("sent"));
                players.sendMessage(this.main.getFileManager().getMessageWithPrefix("receive").replace("%player%", player.getName()));
                this.accept.put(players, player);
                this.main.getServer().getScheduler().runTaskLaterAsynchronously(this.main, () -> {
                    if (this.accept.containsKey(players) && this.accept.get(players).equals(player)) {
                        player.sendMessage(this.main.getFileManager().getErrorWithPrefix("expired"));
                        players.sendMessage(this.main.getFileManager().getErrorWithPrefix("expired"));
                        this.accept.remove(player);
                    }
                }, this.main.getFileManager().getFile("config").getInt("expired") * 20L);
                return true;
            }
            player.sendMessage(this.main.getFileManager().getErrorWithPrefix("found"));
        } catch (ArrayIndexOutOfBoundsException ignored) {
            player.sendMessage(this.main.getFileManager().getErrorWithPrefix("argus"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        final ArrayList<String> completions = new ArrayList<>();
        if (!(commandSender instanceof Player)) {
            if ("stop".startsWith(args[0].toLowerCase()))
                completions.add("stop");
            return completions;
        }
        if (args.length == 1) {
            if ("accept".startsWith(args[0].toLowerCase()))
                completions.add("accept");
            if ("deny".startsWith(args[0].toLowerCase()))
                completions.add("deny");
            if (commandSender.hasPermission("trader.stop"))
                if ("stop".startsWith(args[0].toLowerCase()))
                    completions.add("stop");
            for (final Player player : this.getPlayerRadius(((Player) commandSender).getLocation(), this.main.getFileManager().getFile("config").getInt("radius"))) {
                if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    if (player.equals(commandSender)) continue;
                    completions.add(player.getName());
                }
            }
            return completions;
        }
        return new ArrayList<>();
    }

    private List<Player> getPlayerRadius(final Location location, final int radius) {
        final List<Player> players = new ArrayList<>();
        for (final Entity entity : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
            if (entity instanceof Player) {
                if (entity.getName().contains("&") || entity.getName().contains("§") || ((Player) entity).isSleepingIgnored() || !((Player) entity).isOnline())
                    continue;
                players.add((Player) entity);
            }
        }
        return players;
    }
}
