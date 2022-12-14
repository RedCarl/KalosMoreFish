package red.kalos.morefish.condition;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EnchantmentCondition implements Condition {
  private final Enchantment enchantment;
  private final int level;

  public EnchantmentCondition(Enchantment enchantment, int level) {
    this.enchantment = enchantment;
    this.level = level;
  }


  public boolean isSatisfying(Player player) {
    ItemStack hand = player.getInventory().getItemInMainHand();
    return (hand != null && hand.containsEnchantment(this.enchantment) && hand
            .getEnchantmentLevel(this.enchantment) >= this.level);
  }
}