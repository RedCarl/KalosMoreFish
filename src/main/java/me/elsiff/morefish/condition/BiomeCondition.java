package me.elsiff.morefish.condition;

import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

public class BiomeCondition implements Condition {
    private final Biome biome;

    public BiomeCondition(Biome biome) {
        this.biome = biome;
    }


    public boolean isSatisfying(Player player) {
        Location loc = player.getLocation();
        return (this.biome == player.getWorld().getBiome(loc.getBlockX(), loc.getBlockZ()));
    }
}