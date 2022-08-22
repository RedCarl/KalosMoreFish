package me.elsiff.morefish.manager;

import me.elsiff.morefish.MoreFish;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class BossBarManager {
    private final MoreFish plugin;
    private BossBar timerBar;

    public BossBarManager(MoreFish plugin) {
        this.plugin = plugin;
    }


    public void createTimerBar(long sec) {
        String title = this.plugin.getLocale().getString("timer-boss-bar").replaceAll("%time%", this.plugin.getTimeString(sec));
        BarColor color = BarColor.valueOf(this.plugin.getConfig().getString("messages.contest-bar-color").toUpperCase());

        this.timerBar = this.plugin.getServer().createBossBar(title, color, BarStyle.SEGMENTED_10);

        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            this.timerBar.addPlayer(player);
        }
    }

    public void removeTimerBar() {
        this.timerBar.removeAll();
        this.timerBar = null;
    }

    public void addPlayer(Player player) {
        this.timerBar.addPlayer(player);
    }

    public void updateTimerBar(long passed, long timer) {
        long left = timer - passed;

        String title = this.plugin.getLocale().getString("timer-boss-bar").replaceAll("%time%", this.plugin.getTimeString(left));

        this.timerBar.setTitle(title);
        this.timerBar.setProgress((double) left / timer);
    }
}