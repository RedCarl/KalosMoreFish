package red.kalos.morefish.condition;

import org.bukkit.entity.Player;

public interface Condition {
  boolean isSatisfying(Player paramPlayer);
}