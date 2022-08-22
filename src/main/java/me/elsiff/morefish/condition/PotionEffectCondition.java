package me.elsiff.morefish.condition;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class PotionEffectCondition implements Condition {
    private final PotionEffectType effectType;
    private final int amplifier;

    public PotionEffectCondition(PotionEffectType effectType, int amplifier) {
        this.effectType = effectType;
        this.amplifier = amplifier;
    }


    public boolean isSatisfying(Player player) {
        return (player.hasPotionEffect(this.effectType) && player
                .getPotionEffect(this.effectType).getAmplifier() >= this.amplifier);
    }
}