package red.kalos.morefish.listener;

import red.kalos.morefish.CaughtFish;
import red.kalos.morefish.CustomFish;
import red.kalos.morefish.MoreFish;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener
        implements Listener {
    public PlayerListener(MoreFish plugin) {
        this.plugin = plugin;
    }
    private final MoreFish plugin;
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (this.plugin.hasBossBar() && this.plugin.getContestManager().hasStarted() && this.plugin.getContestManager().hasTimer()) {
            this.plugin.getBossBarManager().addPlayer(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        CaughtFish fish = this.plugin.getFishManager().getCaughtFish(event.getItem());

        if (fish != null) {
            CustomFish.FoodEffects effects = fish.getFoodEffects();

            boolean cancel = false;

            if (effects.hasPoints()) {
                cancel = true;

                int foodLevel = event.getPlayer().getFoodLevel() + effects.getPoints();

                if (foodLevel < 0)
                    foodLevel = 0;
                if (foodLevel > 20) {
                    foodLevel = 20;
                }
                event.getPlayer().setFoodLevel(foodLevel);
            }

            if (effects.hasSaturation()) {
                cancel = true;

                event.getPlayer().setSaturation(effects.getSaturation());
            }

            if (effects.hasCommands()) {
                for (String command : effects.getCommands()) {
                    String str = command.replaceAll("@p", event.getPlayer().getName());

                    str = ChatColor.translateAlternateColorCodes('&', str);

                    this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), str);
                }
            }


            if (cancel) {
                event.setCancelled(true);

                int amount = event.getItem().getAmount();

                if (amount == 1) {
                    event.getPlayer().getInventory().remove(event.getItem());
                } else {
                    event.getItem().setAmount(amount - 1);
                }
            }
        }
    }
}