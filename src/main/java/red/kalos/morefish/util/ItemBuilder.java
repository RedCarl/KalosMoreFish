package red.kalos.morefish.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemBuilder
{
  private ItemStack item;

  public ItemBuilder(Material type) {
    this.item = new ItemStack(type);
  }

  public ItemBuilder(Material type, int amount) {
    this.item = new ItemStack(type, amount);
  }

  public ItemBuilder(Material type, int amount, short durability) {
    this.item = new ItemStack(type, amount, durability);
  }

  public ItemBuilder(ItemStack item) {
    this.item = item;
  }

  public ItemBuilder setDisplayName(String displayName) {
    ItemMeta meta = this.item.getItemMeta();
    meta.setDisplayName(displayName);
    this.item.setItemMeta(meta);
    return this;
  }

  public ItemBuilder addLore(String... lore) {
    ItemMeta meta = this.item.getItemMeta();
    List<String> list = meta.hasLore() ? meta.getLore() : new ArrayList<>();

    Collections.addAll(list, lore);

    meta.setLore(list);
    this.item.setItemMeta(meta);
    return this;
  }

  public ItemBuilder setLore(List<String> lore) {
    ItemMeta meta = this.item.getItemMeta();
    meta.setLore(lore);
    this.item.setItemMeta(meta);
    return this;
  }

  public ItemBuilder addEnchantment(Enchantment ench, int level) {
    this.item.addUnsafeEnchantment(ench, level);
    return this;
  }

  public ItemBuilder addItemFlags(ItemFlag... flags) {
    ItemMeta meta = this.item.getItemMeta();
    meta.addItemFlags(flags);
    this.item.setItemMeta(meta);
    return this;
  }

  public ItemBuilder hideAll() {
    return addItemFlags(ItemFlag.values());
  }

  public ItemStack build() {
    return this.item;
  }
}