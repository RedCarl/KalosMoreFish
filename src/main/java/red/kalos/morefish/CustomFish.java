package red.kalos.morefish;

import java.util.List;
import red.kalos.morefish.condition.Condition;
import org.bukkit.inventory.ItemStack;


public class CustomFish
{
    private final String internalName;
    private final String name;
    private final double lengthMin;
    private final double lengthMax;
    private final ItemStack icon;
    private final boolean skipItemFormat;
    private final List<String> commands;
    private final FoodEffects foodEffects;
    private final List<Condition> conditions;
    private final Rarity rarity;

    public CustomFish(String internalName, String name, double lengthMin, double lengthMax, ItemStack icon, boolean skipItemFormat, List<String> commands, FoodEffects foodEffects, List<Condition> conditions, Rarity rarity) {
        this.internalName = internalName;
        this.name = name;
        this.lengthMin = lengthMin;
        this.lengthMax = lengthMax;
        this.icon = icon;
        this.skipItemFormat = skipItemFormat;
        this.commands = commands;
        this.foodEffects = foodEffects;
        this.conditions = conditions;
        this.rarity = rarity;
    }

    public String getInternalName() {
        return this.internalName;
    }

    public String getName() {
        return this.name;
    }

    public double getLengthMin() {
        return this.lengthMin;
    }

    public double getLengthMax() {
        return this.lengthMax;
    }

    public ItemStack getIcon() {
        return this.icon.clone();
    }

    public boolean hasNoItemFormat() {
        return this.skipItemFormat;
    }

    public List<String> getCommands() {
        return this.commands;
    }

    public FoodEffects getFoodEffects() {
        return this.foodEffects;
    }

    public List<Condition> getConditions() {
        return this.conditions;
    }

    public Rarity getRarity() {
        return this.rarity;
    }





    public static class FoodEffects
    {
        private int points = -1;
        private float saturation = -1.0F;
        private List<String> commands = null;


        public boolean hasPoints() {
            return (this.points != -1);
        }

        public boolean hasSaturation() {
            return (this.saturation > 0.0F);
        }

        public boolean hasCommands() {
            return (this.commands != null);
        }

        public int getPoints() {
            return this.points;
        }

        public void setPoints(int points) {
            this.points = points;
        }

        public float getSaturation() {
            return this.saturation;
        }

        public void setSaturation(float saturation) {
            this.saturation = saturation;
        }

        public List<String> getCommands() {
            return this.commands;
        }

        public void setCommands(List<String> commands) {
            this.commands = commands;
        }
    }
}