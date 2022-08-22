package red.kalos.morefish;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import red.kalos.morefish.command.GeneralCommands;
import red.kalos.morefish.hooker.VaultHooker;
import red.kalos.morefish.manager.BossBarManager;
import red.kalos.morefish.manager.ContestManager;
import red.kalos.morefish.manager.FishManager;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import red.kalos.morefish.listener.*;
import red.kalos.morefish.util.ColorParser;

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

        log(getName() + " " + getDescription().getVersion() + " &7开始加载...");

        long startTime = System.currentTimeMillis();

        updateConfigFiles();

        this.rewardsGUI = new RewardsGUI(this);
        this.fishManager = new FishManager(this);
        this.contestManager = new ContestManager(this);


        if (getConfig().getBoolean("general.use-boss-bar") && Material.getMaterial("SHIELD") != null) {
            this.bossBarManager = new BossBarManager(this);
        }

        log("正在注册指令...");
        regCommand("morefish",new GeneralCommands(this));

        log("正在注册监听器...");
        this.manager = getServer().getPluginManager();
        regListener(new FishingListener(this));
        regListener(new PlayerListener(this));
        regListener(this.rewardsGUI);

        if (this.manager.getPlugin("Vault") != null && this.manager.getPlugin("Vault").isEnabled()) {
            this.vaultHooker = new VaultHooker(this);
            if (this.vaultHooker.setupEconomy()) {
                log("正在加载 Vault 依赖...");
            } else {
                log("未找到 Vault 依赖...");
                this.vaultHooker = null;
            }
        }

        loadFishShop();
        scheduleAutoRunning();

        log("加载完成 ，共耗时 " + (System.currentTimeMillis() - startTime) + " ms 。");

        showAD();
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
        log(getName() + " " + getDescription().getVersion() + " 开始卸载...");
        long startTime = System.currentTimeMillis();

        log("正在保存数据...");
        if (getConfig().getBoolean("general.save-records")) {
            this.contestManager.saveRecords();
        }

        log("卸载监听器...");
        Bukkit.getServicesManager().unregisterAll(this);

        log("卸载完成 ，共耗时 " + (System.currentTimeMillis() - startTime) + " ms 。");

        showAD();
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

    /**
     * 注册监听器
     *
     * @param listener 监听器
     */
    public static void regListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, getInstance());
    }

    /**
     * 注册指令
     *
     * @param name 指令名字
     * @param command 指令
     */
    public static void regCommand(String name, CommandExecutor command) {
        Bukkit.getPluginCommand(name).setExecutor(command);
    }

    /**
     * 日志
     * @param message 日志消息
     */
    public static void log(String message) {
        Bukkit.getConsoleSender().sendMessage(ColorParser.parse("[" + getInstance().getName() + "] " + message));
    }

    /**
     * 作者信息
     */
    private void showAD() {
        log("&7感谢您使用 &c&l"+getDescription().getName()+" v" + getDescription().getVersion());
        log("&7本插件由 &c&lKalos Studios &7提供长期支持与维护。");
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