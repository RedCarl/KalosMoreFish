package red.kalos.morefish.listener;

import red.kalos.morefish.CaughtFish;
import red.kalos.morefish.MoreFish;
import red.kalos.morefish.event.PlayerCatchCustomFishEvent;
import red.kalos.morefish.manager.ContestManager;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

public class FishingListener implements Listener {
    private final MoreFish plugin;

    public FishingListener(MoreFish plugin) {
        this.plugin = plugin;
        this.contest = plugin.getContestManager();
    }

    private final ContestManager contest;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH && event.getCaught() instanceof Item) {
            if (!this.contest.hasStarted() && this.plugin.getConfig().getBoolean("general.no-fishing-unless-contest")) {
                event.setCancelled(true);

                String msg = this.plugin.getLocale().getString("no-fishing-allowed");
                event.getPlayer().sendMessage(msg);

                return;
            }
            if (!hasEnabled(event)) {
                return;
            }

            CaughtFish fish = this.plugin.getFishManager().generateRandomFish(event.getPlayer());


            PlayerCatchCustomFishEvent customEvent = new PlayerCatchCustomFishEvent(event.getPlayer(), fish, event);
            this.plugin.getServer().getPluginManager().callEvent(customEvent);

            if (customEvent.isCancelled()) {
                return;
            }


            String msgFish = getMessage("catch-fish", event.getPlayer(), fish);
            int ancFish = this.plugin.getConfig().getInt("messages.announce-catch");

            if (fish.getRarity().isNoBroadcast()) {
                ancFish = 0;
            }

            announceMessage(event.getPlayer(), msgFish, ancFish);


            if (fish.getRarity().hasFirework()) {
                launchFirework(event.getPlayer().getLocation().add(0.0D, 1.0D, 0.0D));
            }


            if (!fish.getCommands().isEmpty()) {
                executeCommands(event.getPlayer(), fish);
            }


            if (this.contest.hasStarted()) {
                if (this.contest.isNew1st(fish)) {
                    String msgContest = getMessage("get-1st", event.getPlayer(), fish);
                    int ancContest = this.plugin.getConfig().getInt("messages.announce-new-1st");

                    announceMessage(event.getPlayer(), msgContest, ancContest);
                }

                this.contest.addRecord(event.getPlayer(), fish);
            }


            ItemStack itemStack = this.plugin.getFishManager().getItemStack(fish, event.getPlayer().getName());
            Item caught = (Item)event.getCaught();
            caught.setItemStack(itemStack);
        }
    }

    private boolean hasEnabled(PlayerFishEvent event) {
        boolean enabled = !this.plugin.getConfig().getStringList("general.contest-disabled-worlds")
                .contains(event.getPlayer().getWorld().getName());


        if (this.plugin.getConfig().getBoolean("general.only-for-contest") &&
                !this.contest.hasStarted()) {
            enabled = false;
        }

        if (this.plugin.getConfig().getBoolean("general.replace-only-fish") && ((Item)event
                .getCaught()).getItemStack().getType() != Material.RAW_FISH) {
            enabled = false;
        }
        return enabled;
    }

    private String getMessage(String path, Player player, CaughtFish fish) {
        String message = this.plugin.getLocale().getString(path);






        message = message.replaceAll("%player%", player.getName()).replaceAll("%length%", fish.getLength() + "").replaceAll("%rarity%", fish.getRarity().getDisplayName()).replaceAll("%rarity_color%", fish.getRarity().getColor() + "").replaceAll("%fish%", fish.getName()).replaceAll("%fish_with_rarity%", (fish.getRarity().isNoDisplay() ? "" : (fish.getRarity().getDisplayName() + " ")) + fish.getName());

        message = ChatColor.translateAlternateColorCodes('&', message);

        return message;
    }

    private void announceMessage(Player player, String message, int announce) {
        if (message.length() == 0) {
            return;
        }
        switch (announce) {
            case -1:
                this.plugin.getServer().broadcastMessage(message);
                return;
            case 0:
                player.sendMessage(message);
                return;
        }
        Location loc = player.getLocation();

        for (Player other : player.getWorld().getPlayers()) {
            if (other.getLocation().distance(loc) <= announce) {
                other.sendMessage(message);
            }
        }
    }


    private void executeCommands(Player player, CaughtFish fish) {
        for (String command : fish.getCommands()) {


            String str = command.replaceAll("@p", player.getName()).replaceAll("%fish%", fish.getName()).replaceAll("%length%", fish.getLength() + "");

            str = ChatColor.translateAlternateColorCodes('&', str);

            this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), str);
        }
    }

    private void launchFirework(Location loc) {
        Firework firework = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        FireworkEffect effect = FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(Color.AQUA).withFade(Color.BLUE).withTrail().withFlicker().build();
        meta.addEffect(effect);
        meta.setPower(1);
        firework.setFireworkMeta(meta);
    }
}