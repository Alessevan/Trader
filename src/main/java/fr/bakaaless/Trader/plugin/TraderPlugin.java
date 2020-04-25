package fr.bakaaless.Trader.plugin;

import fr.bakaaless.Trader.commands.Executor;
import fr.bakaaless.Trader.object.Trader;
import fr.bakaaless.Trader.utils.FileManager;
import lombok.AccessLevel;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

public class TraderPlugin extends JavaPlugin {

    @Getter(AccessLevel.PUBLIC)
    private static TraderPlugin instance;

    @Getter(AccessLevel.PUBLIC)
    private FileManager fileManager;

    @Getter(AccessLevel.PUBLIC)
    private Optional<Economy> econ;

    @Getter(AccessLevel.PRIVATE)
    private List<Trader> traders;

    @Override
    public void onEnable() {
        super.onEnable();
        if (!this.getServer().getPluginManager().isPluginEnabled("Vault")) {
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        TraderPlugin.instance = this;
        try {
            this.fileManager = new FileManager(this, "messages", "config");
        } catch (IOException | InvalidConfigurationException e) {
            this.getLogger().log(Level.SEVERE, ChatColor.translateAlternateColorCodes('&', "§c§lTrading §4§l» §cError while retrieve creating files."));
        }
        new Executor();
        if (!setupEconomy()) {
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.traders = new ArrayList<>();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        econ = Optional.ofNullable(rsp != null ? rsp.getProvider() : null);
        return econ.isPresent();
    }

    public void addTraders(final Trader... traders) {
        for (final Trader trader : traders) this.getTraders().add(trader);
    }

    public void removeTraders(final Trader... traders) {
        for (final Trader trader : traders) this.getTraders().remove(trader);
    }

    public void stopAll() {
        this.getTraders().forEach(Trader::stop);
    }
}
