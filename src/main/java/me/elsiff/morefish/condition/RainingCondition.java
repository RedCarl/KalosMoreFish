package me.elsiff.morefish.condition;

import org.bukkit.entity.Player;

public class RainingCondition implements Condition {
    private final boolean raining;

    public RainingCondition(boolean raining) {
        this.raining = raining;
    }


    public boolean isSatisfying(Player player) {
        return (this.raining == player.getWorld().hasStorm());
    }
}