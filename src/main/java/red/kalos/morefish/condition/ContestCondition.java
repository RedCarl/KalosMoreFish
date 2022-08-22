package red.kalos.morefish.condition;

import red.kalos.morefish.MoreFish;
import org.bukkit.entity.Player;

public class ContestCondition implements Condition {
    private final boolean ongoing;

    public ContestCondition(boolean ongoing) {
        this.ongoing = ongoing;
    }


    public boolean isSatisfying(Player player) {
        return (this.ongoing == MoreFish.getInstance().getContestManager().hasStarted());
    }
}