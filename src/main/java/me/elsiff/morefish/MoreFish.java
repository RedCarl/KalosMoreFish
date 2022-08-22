package me.elsiff.morefish;

import me.elsiff.morefish.command.GeneralCommands;
import me.elsiff.morefish.hooker.VaultHooker;
import me.elsiff.morefish.listener.*;
import me.elsiff.morefish.manager.BossBarManager;
import me.elsiff.morefish.manager.ContestManager;
import me.elsiff.morefish.manager.FishManager;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MoreFish extends JavaPlugin {
    private int taskId = -1;
    private static MoreFish instance;
    private PluginManager manager;
    private Locale locale;
    private RewardsGUI rewardsGUI;
    private FishShopGUI fishShopGUI;
    private FishManager fishManager;
    private ContestManager contestManager;
    private BossBarManager bossBarManager;
    private VaultHooker vaultHooker;

    public static MoreFish getInstance() {
        return instance;
    }


    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        this.locale = new Locale(this);

        updateConfigFiles();

        this.rewardsGUI = new RewardsGUI(this);
        this.fishManager = new FishManager(this);
        this.contestManager = new ContestManager(this);


        if (getConfig().getBoolean("general.use-boss-bar") && Material.getMaterial("SHIELD") != null) {
            this.bossBarManager = new BossBarManager(this);
        }

        getCommand("morefish").setExecutor(new GeneralCommands(this));

        this.manager = getServer().getPluginManager();
        this.manager.registerEvents(new FishingListener(this), this);
        this.manager.registerEvents(new PlayerListener(this), this);
        this.manager.registerEvents(this.rewardsGUI, this);

        if (this.manager.getPlugin("Vault") != null && this.manager.getPlugin("Vault").isEnabled()) {
            this.vaultHooker = new VaultHooker(this);

            if (this.vaultHooker.setupEconomy()) {
                getLogger().info("Found Vault for economy support.");
            } else {
                this.vaultHooker = null;
            }
        }

        loadFishShop();
        scheduleAutoRunning();

        getLogger().info("Plugin has been enabled!");
    }

    private void updateConfigFiles() {
        String msg = this.locale.getString("old-file");
        ConsoleCommandSender console = getServer().getConsoleSender();

        if (getConfig().getInt("version") != 210)
        {
            console.sendMessage(String.format(msg, "config.yml"));
        }

        if (this.locale.getLangVersion() != 211)
        {
            console.sendMessage(String.format(msg, this.locale.getLangPath()));
        }

        if (this.locale.getFishVersion() != 212)
        {
            console.sendMessage(String.format(msg, this.locale.getFishPath()));
        }
    }

    public void loadFishShop() {
        if (this.fishShopGUI != null) {
            return;
        }
        if (hasEconomy() && getConfig().getBoolean("fish-shop.enable")) {
            this.fishShopGUI = new FishShopGUI(this);
            this.manager.registerEvents(new SignListener(this), this);
            this.manager.registerEvents(this.fishShopGUI, this);
        }
    }

    public void scheduleAutoRunning() {
        if (this.taskId != -1) {
            getServer().getScheduler().cancelTask(this.taskId);
        }
        if (getConfig().getBoolean("auto-running.enable")) {
            final int required = getConfig().getInt("auto-running.required-players");
            final long timer = getConfig().getLong("auto-running.timer");
            final List<String> startTime = getConfig().getStringList("auto-running.start-time");
            final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

            this.taskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
                String now = dateFormat.format(new Date());

                if (startTime.contains(now) && MoreFish.this.getServer().getOnlinePlayers().size() >= required) {
                    MoreFish.this.getServer().dispatchCommand(MoreFish.this.getServer().getConsoleSender(), "morefish start " + timer);
                }
            },0L, 1200L);
        }
    }


    public void onDisable() {
        if (getConfig().getBoolean("general.save-records")) {
            this.contestManager.saveRecords();
        }
        getLogger().info("Plugin has been disabled!");
    }

    public Locale getLocale() {
        return this.locale;
    }

    public FishManager getFishManager() {
        return this.fishManager;
    }

    public ContestManager getContestManager() {
        return this.contestManager;
    }

    public BossBarManager getBossBarManager() {
        return this.bossBarManager;
    }

    public boolean hasBossBar() {
        return (getBossBarManager() != null);
    }

    public String getOrdinal(int number) {
        switch (number) {
            case 1:
                return "1st";
            case 2:
                return "2nd";
            case 3:
                return "3rd";
        }
        if (number > 20) {
            return (number / 10) + getOrdinal(number % 10);
        }
        return number + "th";
    }



    public String getTimeString(long sec) {
        StringBuilder builder = new StringBuilder();

        int minutes = (int)(sec / 60L);
        int second = (int)(sec - (minutes * 60));

        if (minutes > 0) {
            builder.append(minutes);
            builder.append(getLocale().getString("time-format-minutes"));
            builder.append(" ");
        }

        builder.append(second);
        builder.append(getLocale().getString("time-format-seconds"));

        return builder.toString();
    }

    public RewardsGUI getRewardsGUI() {
        return this.rewardsGUI;
    }

    public FishShopGUI getFishShopGUI() {
        return this.fishShopGUI;
    }

    public boolean hasEconomy() {
        return (this.vaultHooker != null);
    }

    public VaultHooker getVaultHooker() {
        return this.vaultHooker;
    }
}