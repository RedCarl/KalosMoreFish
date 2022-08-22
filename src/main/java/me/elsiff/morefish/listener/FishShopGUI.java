package me.elsiff.morefish.listener;

import com.google.common.math.DoubleMath;
import me.elsiff.morefish.CaughtFish;
import me.elsiff.morefish.MoreFish;
import me.elsiff.morefish.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FishShopGUI implements Listener {
    private final Set<UUID> users = new HashSet<>(); private final MoreFish plugin;

    public FishShopGUI(MoreFish plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player) {
        String title = this.plugin.getLocale().getString("shop-gui-title");
        Inventory inv = this.plugin.getServer().createInventory(player, 36, title);

        ItemStack iconGlass = (new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (short)3)).setDisplayName("§r").build();
        for (int i = 0; i < 9; i++) {
            inv.setItem(27 + i, iconGlass);
        }

        updateEmeraldIcon(inv);

        player.openInventory(inv);
        this.users.add(player.getUniqueId());
    }



    private void updateEmeraldIcon(Inventory inv) {
        double price = getTotalPrice(inv);
        String priceStr = getPriceString(price);

        String displayName = this.plugin.getLocale().getString("shop-emerald-icon-name").replaceAll("%price%", priceStr);



        ItemStack iconEmerald = (new ItemBuilder(Material.EMERALD)).setDisplayName(displayName).build();
        inv.setItem(31, iconEmerald);
    }

    private double getTotalPrice(Inventory inv) {
        double total = 0.0D;

        for (int i = 0; i < 27; i++) {
            ItemStack itemStack = inv.getItem(i);

            if (itemStack != null && itemStack.getType() != Material.AIR && this.plugin
                    .getFishManager().isCustomFish(itemStack)) {



                CaughtFish fish = this.plugin.getFishManager().getCaughtFish(itemStack);
                double multiplier = this.plugin.getConfig().getDouble("fish-shop.multiplier");
                double additionalPrice = fish.getRarity().getAdditionalPrice();
                double price = fish.getLength() * multiplier + additionalPrice;

                if (price < 0.0D) {
                    price = 0.0D;
                }

                if (this.plugin.getConfig().getBoolean("fish-shop.round-decimal-points")) {
                    price = (int)price;
                }

                total += price * itemStack.getAmount();
            }
        }
        return total;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (this.users.contains(event.getWhoClicked().getUniqueId())) {
            event.getInventory().setItem(31, new ItemStack(Material.AIR));
            final Inventory inv = event.getInventory();
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> FishShopGUI.this.updateEmeraldIcon(inv),  1L);

            if (27 <= event.getRawSlot() && event.getRawSlot() <= 35) {
                event.setCancelled(true);

                if (event.getRawSlot() == 31) {
                    Player player = (Player)event.getWhoClicked();
                    double price = getTotalPrice(event.getInventory());

                    if (!this.plugin.hasEconomy() || !this.plugin.getVaultHooker().getEconomy().hasAccount(player)) {
                        return;
                    }

                    boolean sold = false;
                    for (int i = 0; i < 27; i++) {
                        ItemStack itemStack = event.getInventory().getItem(i);

                        if (itemStack != null && itemStack.getType() != Material.AIR && this.plugin
                                .getFishManager().isCustomFish(itemStack)) {



                            event.getInventory().setItem(i, new ItemStack(Material.AIR));
                            sold = true;
                        }
                    }
                    if (!sold) {
                        player.sendMessage(this.plugin.getLocale().getString("shop-no-fish"));

                        return;
                    }
                    this.plugin.getVaultHooker().getEconomy().depositPlayer(player, price);

                    String priceStr = getPriceString(price);
                    player.sendMessage(this.plugin.getLocale().getString("shop-sold")
                            .replaceAll("%price%", priceStr + ""));
                }
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (this.users.contains(event.getWhoClicked().getUniqueId())) {
            event.getInventory().setItem(31, new ItemStack(Material.AIR));
            final Inventory inv = event.getInventory();
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> FishShopGUI.this.updateEmeraldIcon(inv),  1L);
        }
    }

    private String getPriceString(double price) {
        return DoubleMath.isMathematicalInteger(price) ? Integer.toString((int)price) : Double.toString(price);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (this.users.contains(event.getPlayer().getUniqueId())) {
            this.users.remove(event.getPlayer().getUniqueId());

            for (int i = 0; i < 27; i++) {
                ItemStack itemStack = event.getInventory().getItem(i);

                if (itemStack != null && itemStack.getType() != Material.AIR && this.plugin
                        .getFishManager().isCustomFish(itemStack))
                {


                    if (event.getPlayer().getInventory().firstEmpty() == -1) {
                        event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(), itemStack);
                    } else {
                        event.getPlayer().getInventory().addItem(itemStack);
                    }
                }
            }
        }
    }
}