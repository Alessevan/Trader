package fr.bakaaless.Trader.plugin;

import fr.bakaaless.Trader.commands.Executor;
import fr.bakaaless.Trader.object.Trader;
import fr.bakaaless.Trader.utils.FileManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

public class TraderPlugin extends JavaPlugin {

    private static TraderPlugin instance;

    private FileManager fileManager;

    private Economy econ;

    private List<Trader> traders;

    @Override
    public void onEnable() {
        super.onEnable();
        if (!this.getServer().getPluginManager().isPluginEnabled("Vault")) {
            this.getLogger().log(Level.SEVERE, ChatColor.translateAlternateColorCodes('&', "§c§lTrading §4§l» §cVault isn't detected. Aborted."));
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        try {
            this.fileManager = new FileManager(this, "messages", "config");
        } catch (IOException | InvalidConfigurationException e) {
            this.getLogger().log(Level.SEVERE, ChatColor.translateAlternateColorCodes('&', "§c§lTrading §4§l» §cError while retrieve creating files. Aborted."));
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!setupEconomy()) {
            this.getLogger().log(Level.SEVERE, ChatColor.translateAlternateColorCodes('&', "§c§lTrading §4§l» §cNo economy system detected. Aborted."));
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        TraderPlugin.instance = this;
        new Executor();
        this.traders = new ArrayList<>();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        econ = rsp != null ? rsp.getProvider() : null;
        return econ != null;
    }

    public void addTraders(final Trader... traders) {
        this.traders.addAll(Arrays.asList(traders));
    }

    public void removeTraders(final Trader... traders) {
        for (final Trader trader : traders) this.traders.remove(trader);
    }

    public void stopAll() {
        this.traders.forEach(Trader::stop);
    }

    public static TraderPlugin getInstance() {
        return instance;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public Economy getEcon() {
        return econ;
    }
}
