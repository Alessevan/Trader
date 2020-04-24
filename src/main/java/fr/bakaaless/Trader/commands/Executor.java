package fr.bakaaless.Trader.commands;

import fr.bakaaless.Trader.object.Trader;
import fr.bakaaless.Trader.plugin.TraderPlugin;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Executor implements CommandExecutor, TabCompleter {

    @Getter(AccessLevel.PRIVATE)
    private final TraderPlugin main;

    @Getter(AccessLevel.PRIVATE)
    private final Map<Player, Player> accept;

    public Executor() {
        this.main = TraderPlugin.getInstance();
        this.accept = new HashMap<>();
        this.getMain().getCommand("trader").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        final Player player = (Player) sender;
        try {
            if (args[0].equalsIgnoreCase("accept")) {
                if (!this.getAccept().containsKey(player)) {
                    player.sendMessage(this.getMain().getFileManager().getErrorWithPrefix("none"));
                    return true;
                }
                player.sendMessage(this.getMain().getFileManager().getMessageWithPrefix("accept"));
                this.getAccept().get(player).sendMessage(this.getMain().getFileManager().getMessageWithPrefix("accept"));
                this.getMain().addTraders(new Trader(player, this.getAccept().get(player)));
                this.getAccept().remove(player);
                return true;
            } else if (args[0].equalsIgnoreCase("deny")) {
                if (!this.getAccept().containsKey(player)) {
                    player.sendMessage(this.getMain().getFileManager().getErrorWithPrefix("none"));
                    return true;
                }
                player.sendMessage(this.getMain().getFileManager().getMessageWithPrefix("deny"));
                this.getAccept().get(player).sendMessage(this.getMain().getFileManager().getErrorWithPrefix("deny"));
                this.getAccept().remove(player);
                return true;
            } else if (args[0].equalsIgnoreCase("stop")) {
                if (player.hasPermission("trader.stop")) {
                    this.getMain().stopAll();
                    player.sendMessage(this.getMain().getFileManager().getMessageWithPrefix("stop"));
                    return true;
                }
            }
            for (final Player players : this.getMain().getServer().getOnlinePlayers()) {
                if (!players.getName().toLowerCase().equalsIgnoreCase(args[0])) continue;
                if (player.equals(players)) {
                    player.sendMessage(this.getMain().getFileManager().getErrorWithPrefix("yourself"));
                    return true;
                }
                if (this.getAccept().containsKey(player) && this.getAccept().get(player).equals(players)) {
                    return player.performCommand("trader accept");
                }
                player.sendMessage(this.getMain().getFileManager().getMessageWithPrefix("sent"));
                players.sendMessage(this.getMain().getFileManager().getMessageWithPrefix("receive").replace("%player%", player.getName()));
                this.getAccept().put(players, player);
                this.getMain().getServer().getScheduler().runTaskLaterAsynchronously(this.getMain(), () -> {
                    if (this.getAccept().containsKey(players) && this.getAccept().get(players).equals(player)) {
                        player.sendMessage(this.getMain().getFileManager().getErrorWithPrefix("expired"));
                        players.sendMessage(this.getMain().getFileManager().getErrorWithPrefix("expired"));
                        this.getAccept().remove(player);
                    }
                }, 120L * 20L);
                return true;
            }
            player.sendMessage(this.getMain().getFileManager().getErrorWithPrefix("found"));
        } catch (ArrayIndexOutOfBoundsException ignored) {
            player.sendMessage(this.getMain().getFileManager().getErrorWithPrefix("argus"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        final ArrayList<String> completions = new ArrayList<>();
        if (args.length == 1) {
            if ("accept".startsWith(args[0].toLowerCase()))
                completions.add("accept");
            if ("deny".startsWith(args[0].toLowerCase()))
                completions.add("deny");
            if (commandSender.hasPermission("trader.stop"))
                if ("stop".startsWith(args[0].toLowerCase()))
                    completions.add("stop");
            for (final Player player : this.getMain().getServer().getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    if (player.equals(commandSender)) continue;
                    completions.add(player.getName());
                }
            }
            return completions;
        }
        return new ArrayList<>();
    }
}
