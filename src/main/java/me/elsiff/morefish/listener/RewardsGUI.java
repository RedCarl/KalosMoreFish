package me.elsiff.morefish.listener;

import me.elsiff.morefish.MoreFish;
import me.elsiff.morefish.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class RewardsGUI implements Listener {
    private final Set<UUID> users = new HashSet<>(); private final MoreFish plugin;
    private final Map<UUID, Integer> editors = new HashMap<>();

    public RewardsGUI(MoreFish plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player) {
        String title = this.plugin.getLocale().getString("rewards-gui-title");
        Inventory inv = this.plugin.getServer().createInventory(player, this.plugin.hasEconomy() ? 18 : 9, title);

        ItemStack[] rewards = this.plugin.getContestManager().getRewards();

        for (int i = 0; i < 8 && i < rewards.length; i++) {
            inv.setItem(i, rewards[i]);
        }




        ItemStack iconGuide = (new ItemBuilder(Material.SIGN)).setDisplayName(this.plugin.getLocale().getString("rewards-guide-icon-name")).setLore(this.plugin.getLocale().getStringList("rewards-guide-icon-lore")).build();

        inv.setItem(8, iconGuide);

        if (this.plugin.hasEconomy()) {
            double[] cashPrizes = this.plugin.getContestManager().getCashPrizes();

            for (int j = 0; j < 8; j++) {
                double amount = cashPrizes[j];

                String ordinal = this.plugin.getOrdinal(j + 1);
                String number = Integer.toString(j + 1);

                if (j == 7) {
                    String text = this.plugin.getLocale().getString("rewards-consolation");
                    ordinal = text;
                    number = text;
                }







                ItemStack iconEmerald = (new ItemBuilder(Material.EMERALD)).setDisplayName(this.plugin.getLocale().getString("rewards-emerald-icon-name").replaceAll("%ordinal%", ordinal).replaceAll("%number%", number).replaceAll("%amount%", Double.toString(amount))).setLore(this.plugin.getLocale().getStringList("rewards-emerald-icon-lore")).build();

                inv.setItem(9 + j, iconEmerald);
            }




            ItemStack iconMoneyGuide = (new ItemBuilder(Material.SIGN)).setDisplayName(this.plugin.getLocale().getString("rewards-sign-icon-name")).setLore(this.plugin.getLocale().getStringList("rewards-sign-icon-lore")).build();

            inv.setItem(17, iconMoneyGuide);
        }

        player.openInventory(inv);
        this.users.add(player.getUniqueId());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (this.users.contains(event.getWhoClicked().getUniqueId())) {
            if (event.getRawSlot() < 9 || event.getInventory().getSize() < event.getRawSlot()) {
                return;
            }

            event.setCancelled(true);

            if (9 <= event.getRawSlot() && event.getRawSlot() <= 16 && this.plugin.hasEconomy()) {
                this.editors.put(event.getWhoClicked().getUniqueId(), event.getRawSlot() - 9);

                event.getWhoClicked().closeInventory();

                for (String msg : this.plugin.getLocale().getStringList("enter-cash-prize")) {
                    event.getWhoClicked().sendMessage(msg);
                }
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        UUID id = event.getPlayer().getUniqueId();

        if (this.editors.containsKey(id)) {
            double value; event.setCancelled(true);

            if ("cancel".equalsIgnoreCase(event.getMessage())) {
                this.editors.remove(id);
                openGUI(event.getPlayer());

                event.getPlayer().sendMessage(this.plugin.getLocale().getString("entered-cancel"));

                return;
            }

            try {
                value = Double.parseDouble(event.getMessage());
            } catch (NumberFormatException ex) {
                event.getPlayer().sendMessage(String.format(this.plugin.getLocale().getString("entered-not-number"), event.getMessage()));

                return;
            }
            if (value < 0.0D) {
                event.getPlayer().sendMessage(this.plugin.getLocale().getString("entered-not-positive"));

                return;
            }
            int index = this.editors.get(id);

            double[] cashPrizes = this.plugin.getContestManager().getCashPrizes();
            cashPrizes[index] = value;
            this.plugin.getContestManager().setCashPrizes(cashPrizes);

            this.editors.remove(id);
            openGUI(event.getPlayer());

            event.getPlayer().sendMessage(this.plugin.getLocale().getString("entered-successfully"));
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (this.users.contains(event.getPlayer().getUniqueId())) {
            this.users.remove(event.getPlayer().getUniqueId());

            ItemStack[] rewards = new ItemStack[8];

            for (int i = 0; i < 8; i++) {
                ItemStack stack = event.getInventory().getItem(i);

                if (stack != null && stack.getType() != Material.AIR)
                {

                    rewards[i] = stack;
                }
            }
            this.plugin.getContestManager().setRewards(rewards);

            if (!this.editors.containsKey(event.getPlayer().getUniqueId()))
                event.getPlayer().sendMessage(this.plugin.getLocale().getString("saved-changes"));
        }
    }
}