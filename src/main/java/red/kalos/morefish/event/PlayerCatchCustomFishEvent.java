package red.kalos.morefish.event;

import red.kalos.morefish.CaughtFish;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerFishEvent;

public class PlayerCatchCustomFishEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final CaughtFish fish;
    private final PlayerFishEvent fishEvent;

    public PlayerCatchCustomFishEvent(Player who, CaughtFish fish, PlayerFishEvent fishEvent) {
        super(who);
        this.fish = fish;
        this.fishEvent = fishEvent;
    }

    public CaughtFish getFish() {
        return this.fish;
    }

    public PlayerFishEvent getPlayerFishEvent() {
        return this.fishEvent;
    }


    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }


    public boolean isCancelled() {
        return this.cancelled;
    }


    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}