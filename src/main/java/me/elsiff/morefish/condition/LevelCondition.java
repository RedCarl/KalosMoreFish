package me.elsiff.morefish.condition;

import org.bukkit.entity.Player;

public class LevelCondition implements Condition {
  private final int level;

  public LevelCondition(int level) {
    this.level = level;
  }


  public boolean isSatisfying(Player player) {
    return (player.getLevel() >= this.level);
  }
}