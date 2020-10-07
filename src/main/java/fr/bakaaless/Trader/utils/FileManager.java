package fr.bakaaless.Trader.utils;

import fr.bakaaless.Trader.plugin.TraderPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class FileManager {

    private final TraderPlugin main;
    private final Map<String, File> filesMap;
    private final Map<String, YamlConfiguration> yamlMap;

    public FileManager(final TraderPlugin main, final String... files) throws IOException, InvalidConfigurationException {
        this.main = main;
        this.filesMap = new HashMap<>();
        this.yamlMap = new HashMap<>();
        this.init(files);
    }

    public void init(final String... files) throws IOException, InvalidConfigurationException {
        // Récupération / Création des fichiers
        for (String file : files) {
            this.filesMap.put(file, new File(main.getDataFolder(), file + ".yml"));
            if (!this.filesMap.get(file).exists()) {
                this.filesMap.get(file).getParentFile().mkdirs();
                main.saveResource(file + ".yml", false);
            }
            this.yamlMap.put(file, new YamlConfiguration());
            this.yamlMap.get(file).load(this.filesMap.get(file));
        }

    }

    public void reload() throws IOException, InvalidConfigurationException {
        // Récupération / Création des fichiers
        for (final String file : this.filesMap.keySet()) {
            this.filesMap.replace(file, new File(this.main.getDataFolder(), file + ".yml"));
            if (!this.filesMap.get(file).exists()) {
                final boolean b = this.filesMap.get(file).getParentFile().mkdirs();
                if (!b) {
                    this.main.getLogger().log(Level.SEVERE, ChatColor.translateAlternateColorCodes('&', "§c§lTrading §4§l» §cError while retrieve creating file '" + file + "'."));
                    continue;
                }
                main.saveResource(file + ".yml", false);
            }
            this.yamlMap.replace(file, new YamlConfiguration());
            this.yamlMap.get(file).load(this.filesMap.get(file));
        }
    }

    public void setLine(final String file, final String path, final Object value) {
        // On met à jour la valeur
        this.yamlMap.get(file).set(path, value);
        // On save et on recharge le fichier
        try {
            this.yamlMap.get(file).save(this.filesMap.get(file));
            this.yamlMap.get(file).load(this.filesMap.get(file));
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getFile(final String file) {
        return this.yamlMap.get(file);
    }

    public String getMessage(final String file, final String path) {
        // En cas de null exception
        if (this.yamlMap.get(file).getString(path) == null)
            return ChatColor.translateAlternateColorCodes('&', "&cPath not found.");
        // On return en remplaçant les couleurs.
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(this.yamlMap.get(file).getString(path)));
    }

    public String getMessageWithPrefix(final String path) {
        // En cas de null exception
        if (this.yamlMap.get("messages").getString("messages.info." + path) == null)
            return ChatColor.translateAlternateColorCodes('&', "&cPath not found.");
        // On return en remplaçant les couleurs.
        return ChatColor.translateAlternateColorCodes('&', this.yamlMap.get("messages").getString("prefix.info") + this.yamlMap.get("messages").getString("messages.info." + path));
    }

    public String getErrorWithPrefix(final String path) {
        // En cas de null exception
        if (this.yamlMap.get("messages").getString("messages.error." + path) == null)
            return ChatColor.translateAlternateColorCodes('&', "&cPath not found.");
        // On return en remplaçant les couleurs.
        return ChatColor.translateAlternateColorCodes('&', this.yamlMap.get("messages").getString("prefix.error") + this.yamlMap.get("messages").getString("messages.error." + path));
    }
}
