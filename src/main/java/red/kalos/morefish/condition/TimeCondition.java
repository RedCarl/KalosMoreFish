package red.kalos.morefish.condition;

import org.bukkit.entity.Player;

public class TimeCondition implements Condition {
    private final String time;

    public TimeCondition(String time) {
        this.time = time;
    }

    public boolean isSatisfying(Player player) {
        long tick = player.getWorld().getTime();
        switch (this.time) {
            case "day":
                return (1000L <= tick && tick < 13000L);
            case "night":
                return (13000L <= tick || tick < 1000L);
        }
        return false;
    }
}