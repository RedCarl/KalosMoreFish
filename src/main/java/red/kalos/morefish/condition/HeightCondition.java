package red.kalos.morefish.condition;

import org.bukkit.entity.Player;

public class HeightCondition implements Condition {
  private final int minHeight;
  private final int maxHeight;

  public HeightCondition(int minHeight, int maxHeight) {
    this.minHeight = minHeight;
    this.maxHeight = maxHeight;
  }


  public boolean isSatisfying(Player player) {
    int y = player.getLocation().getBlockY();
    return (this.minHeight <= y && y <= this.maxHeight);
  }
}