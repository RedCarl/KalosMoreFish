package red.kalos.morefish.listener;

import red.kalos.morefish.MoreFish;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignListener implements Listener {
    private final MoreFish plugin;

    public SignListener(MoreFish plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        String firstLine = ChatColor.translateAlternateColorCodes('&', event.getLine(0)).replaceAll("§b", "");
        if ("[FishShop]".equalsIgnoreCase(firstLine)) {
            if (!event.getPlayer().hasPermission("morefish.admin")) {
                event.getPlayer().sendMessage(this.plugin.getLocale().getString("no-permission"));

                return;
            }
            event.setLine(0, "§b[FishShop]");
            event.getPlayer().sendMessage(this.plugin.getLocale().getString("created-sign-shop"));
        }
    }

    @EventHandler
    public void onInteracat(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof Sign && this.plugin
                .getFishShopGUI() != null) {
            Sign sign = (Sign)event.getClickedBlock().getState();

            if ("§b[FishShop]".equalsIgnoreCase(sign.getLine(0)))
                this.plugin.getFishShopGUI().openGUI(event.getPlayer());
        }
    }
}