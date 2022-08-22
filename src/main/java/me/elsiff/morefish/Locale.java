package me.elsiff.morefish;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Locale
{
    private final MoreFish plugin;
    private final File folder;
    private final String langPath;
    private final String fishPath;
    private FileConfiguration lang;
    private FileConfiguration fish;

    public Locale(MoreFish plugin) {
        this.plugin = plugin;

        String locale = plugin.getConfig().getString("general.locale");
        this.folder = new File(plugin.getDataFolder(), "locale");
        this.langPath = "lang_" + locale + ".yml";
        this.fishPath = "fish_" + locale + ".yml";

        loadFiles();
    }

    private FileConfiguration loadConfiguration(File folder, String path) throws IOException, InvalidConfigurationException {
        File file = new File(folder, path);

        if (!file.exists()) {
            this.plugin.saveResource("locale\\" + path, false);
        }

        YamlConfiguration config = new YamlConfiguration();
        config.load(file);

        return config;
    }

    public FileConfiguration getLangConfig() {
        return this.lang;
    }

    public FileConfiguration getFishConfig() {
        return this.fish;
    }

    public boolean loadFiles() {
        try {
            this.lang = loadConfiguration(this.folder, this.langPath);
            this.fish = loadConfiguration(this.folder, this.fishPath);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public String getString(String path) {
        String value = this.lang.getString(path);
        return ChatColor.translateAlternateColorCodes('&', value);
    }

    public List<String> getStringList(String path) {
        List<String> list = new ArrayList<>();

        for (String value : this.lang.getStringList(path)) {
            list.add(ChatColor.translateAlternateColorCodes('&', value));
        }

        return list;
    }

    public int getLangVersion() {
        return this.lang.getInt("version");
    }

    public int getFishVersion() {
        return this.fish.getInt("version");
    }

    public String getLangPath() {
        return this.langPath;
    }

    public String getFishPath() {
        return this.fishPath;
    }
}