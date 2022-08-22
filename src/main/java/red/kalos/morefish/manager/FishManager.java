package red.kalos.morefish.manager;
import java.text.SimpleDateFormat;
import java.util.*;

import red.kalos.morefish.CaughtFish;
import red.kalos.morefish.CustomFish;
import red.kalos.morefish.MoreFish;
import red.kalos.morefish.Rarity;
import red.kalos.morefish.condition.*;
import red.kalos.morefish.util.IdentityUtils;
import red.kalos.morefish.util.SkullUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffectType;

public class FishManager {
    private final Random random = new Random(); private final MoreFish plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm");
    private final List<Rarity> rarityList = new ArrayList<>();
    private final Map<String, CustomFish> fishMap = new HashMap<>();
    private final Map<Rarity, List<CustomFish>> rarityMap = new HashMap<>();

    public FishManager(MoreFish plugin) {
        this.plugin = plugin;

        loadFishList();
    }

    public void loadFishList() {
        this.fishMap.clear();
        this.rarityMap.clear();

        FileConfiguration config = this.plugin.getLocale().getFishConfig();

        loadRarities(config);

        for (Rarity rarity : this.rarityList) {
            ConfigurationSection section = config.getConfigurationSection("fish-list." + rarity.getName().toLowerCase());

            for (String path : section.getKeys(false)) {
                CustomFish fish = createCustomFish(section, path, rarity);

                this.fishMap.put(path, fish);
                this.rarityMap.get(rarity).add(fish);
            }
        }

        this.plugin.getLogger().info("Loaded " + this.fishMap.size() + " fish successfully.");
    }

    private void loadRarities(FileConfiguration config) {
        ConfigurationSection rarities = config.getConfigurationSection("rarity-list");
        double totalChance = 0.0D;

        for (String path : rarities.getKeys(false)) {
            String displayName = rarities.getString(path + ".display-name");
            boolean isDefault = (rarities.contains(path + ".default") && rarities.getBoolean(path + ".default"));
            double chance = !isDefault ? (rarities.getDouble(path + ".chance") * 0.01D) : 0.0D;
            ChatColor color = ChatColor.valueOf(rarities.getString(path + ".color").toUpperCase());

            double additionalPrice = rarities.contains(path + ".additional-price") ? rarities.getDouble(path + ".additional-price") : 0.0D;
            boolean noBroadcast = (rarities.contains(path + ".no-broadcast") && rarities.getBoolean(path + ".no-broadcast"));
            boolean noDisplay = (rarities.contains(path + ".no-display") && rarities.getBoolean(path + ".no-display"));
            boolean firework = (rarities.contains(path + ".firework") && rarities.getBoolean(path + ".firework"));

            Rarity rarity = new Rarity(path, displayName, isDefault, chance, color, additionalPrice, noBroadcast, noDisplay, firework);

            this.rarityList.add(rarity);
            totalChance += chance;
        }

        ListIterator<Rarity> it = this.rarityList.listIterator();
        while (it.hasNext()) {
            Rarity rarity = it.next();

            if (rarity.isDefault()) {
                rarity = new Rarity(rarity, 1.0D - totalChance);
                it.set(rarity);
            }

            this.rarityMap.put(rarity, new ArrayList<>());
        }
    }

    private CustomFish createCustomFish(ConfigurationSection section, String path, Rarity rarity) {
        String displayName = section.getString(path + ".display-name");
        double lengthMin = section.getDouble(path + ".length-min");
        double lengthMax = section.getDouble(path + ".length-max");
        ItemStack icon = getIcon(section, path);

        boolean skipItemFormat = (section.contains(path + ".skip-item-format") && section.getBoolean(path + ".skip-item-format"));
        List<String> commands = new ArrayList<>();
        CustomFish.FoodEffects foodEffects = new CustomFish.FoodEffects();
        List<Condition> conditions = new ArrayList<>();

        if (section.contains(path + ".command")) {
            commands = section.getStringList(path + ".command");
        }

        if (section.contains(path + ".food-effects")) {
            if (section.contains(path + ".food-effects.points")) {
                foodEffects.setPoints(section.getInt(path + ".food-effects.points"));
            }

            if (section.contains(path + ".food-effects.saturation")) {
                foodEffects.setSaturation((float)section.getDouble(path + ".food-effects.saturation"));
            }

            if (section.contains(path + ".food-effects.commands")) {
                foodEffects.setCommands(section.getStringList(path + ".food-effects.commands"));
            }
        }

        if (section.contains(path + ".conditions")) {
            List<String> list = section.getStringList(path + ".conditions");

            for (String content : list) {
                Condition condition = getCondition(content);
                conditions.add(condition);
            }
        }

        return new CustomFish(path, displayName, lengthMin, lengthMax, icon, skipItemFormat, commands, foodEffects, conditions, rarity);
    }



    private ItemStack getIcon(ConfigurationSection section, String path) {
        String id = section.getString(path + ".icon.id");
        Material material = IdentityUtils.getMaterial(id);
        if (material == null) {
            this.plugin.getLogger().warning("'" + id + "' is invalid item id!");
            return null;
        }

        int amount = 1;
        if (section.contains(path + ".icon.amount")) {
            amount = section.getInt(path + ".icon.amount");
        }

        short durability = 0;
        if (section.contains(path + ".icon.durability")) {
            durability = (short)section.getInt(path + ".icon.durability");
        }

        ItemStack itemStack = new ItemStack(material, amount, durability);
        ItemMeta meta = itemStack.getItemMeta();

        if (section.contains(path + ".icon.lore")) {
            List<String> lore = new ArrayList<>();
            for (String line : section.getStringList(path + ".icon.lore")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(lore);
        }

        if (section.contains(path + ".icon.enchantments")) {
            for (String content : section.getStringList(path + ".icon.enchantments")) {
                String[] values = content.split("\\|");
                Enchantment ench = IdentityUtils.getEnchantment(values[0].toLowerCase());
                int lv = Integer.parseInt(values[1]);
                meta.addEnchant(ench, lv, true);
            }
        }

        if (section.contains(path + ".icon.unbreakable")) {
            boolean value = section.getBoolean(path + ".icon.unbreakable");
            meta.setUnbreakable(value);
        }

        if (section.contains(path + ".icon.skull-name")) {
            SkullMeta skullMeta = (SkullMeta)meta;
            skullMeta.setOwner(section.getString(path + ".icon.skull-name"));
        }

        itemStack.setItemMeta(meta);

        if (section.contains(path + ".icon.skull-texture")) {
            String value = section.getString(path + ".icon.skull-texture");
            itemStack = SkullUtils.setSkullTexture(itemStack, value);
        }

        return itemStack;
    }
    private Condition getCondition(String content) {
        boolean raining, thundering;
        String time;
        Biome biome;
        Enchantment ench;
        int lv, level;
        boolean ongoing;
        PotionEffectType effectType;
        int amplfier, minHeight, maxHeight;
        String[] values = content.split("\\|");
        String conId = values[0];

        switch (conId) {
            case "raining":
                raining = Boolean.parseBoolean(values[1]);
                return new RainingCondition(raining);

            case "thundering":
                thundering = Boolean.parseBoolean(values[1]);
                return new ThunderingCondition(thundering);

            case "time":
                time = values[1].toLowerCase();
                return new TimeCondition(time);

            case "biome":
                biome = Biome.valueOf(values[1].toUpperCase());
                return new BiomeCondition(biome);

            case "enchantment":
                ench = IdentityUtils.getEnchantment(values[1].toLowerCase());
                lv = Integer.parseInt(values[2]);
                return new EnchantmentCondition(ench, lv);

            case "level":
                level = Integer.parseInt(values[1]);
                return new LevelCondition(level);

            case "contest":
                ongoing = Boolean.parseBoolean(values[1]);
                return new ContestCondition(ongoing);

            case "potioneffect":
                effectType = IdentityUtils.getPotionEffectType(values[1]);
                amplfier = Integer.parseInt(values[2]);
                return new PotionEffectCondition(effectType, amplfier);

            case "height":
                minHeight = Integer.parseInt(values[1]);
                maxHeight = Integer.parseInt(values[2]);
                return new HeightCondition(minHeight, maxHeight);
        }

        return null; }




    public CaughtFish generateRandomFish(Player catcher) {
        Rarity rarity = getRandomRarity();
        CustomFish type = getRandomFish(rarity, catcher);

        return createCaughtFish(type, catcher);
    }

    public CustomFish getCustomFish(String name) {
        return this.fishMap.get(name);
    }

    public ItemStack getItemStack(CaughtFish fish, String fisher) {
        ItemStack itemStack = fish.getIcon();
        ItemMeta meta = itemStack.getItemMeta();

        if (!fish.hasNoItemFormat()) {
            FileConfiguration config = this.plugin.getLocale().getFishConfig();

            String displayName = config.getString("item-format.display-name").replaceAll("%player%", fisher).replaceAll("%rarity%", fish.getRarity().getDisplayName()).replaceAll("%rarity_color%", fish.getRarity().getColor() + "").replaceAll("%fish%", fish.getName());
            displayName = ChatColor.translateAlternateColorCodes('&', displayName);

            try {
                String data = encodeFishData(fish);
                meta.setDisplayName(displayName + data);
            }catch (NullPointerException e){
                System.out.println(displayName +" "+encodeFishData(fish));
            }

            List<String> lore = new ArrayList<>();
            for (String str : config.getStringList("item-format.lore")) {

                String line = str.replaceAll("%player%", fisher).replaceAll("%rarity%", fish.getRarity().getDisplayName()).replaceAll("%rarity_color%", fish.getRarity().getColor() + "").replaceAll("%length%", fish.getLength() + "").replaceAll("%fish%", fish.getName()).replaceAll("%date%", this.dateFormat.format(new Date()));

                line = ChatColor.translateAlternateColorCodes('&', line);
                lore.add(line);
            }
            if (meta.hasLore()) {
                lore.addAll(meta.getLore());
            }
            meta.setLore(lore);
        }

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public CaughtFish getCaughtFish(ItemStack itemStack) {
        if (!itemStack.hasItemMeta() || !itemStack.getItemMeta().hasDisplayName()) {
            return null;
        }

        String displayName = itemStack.getItemMeta().getDisplayName();
        return decodeFishData(displayName);
    }

    public boolean isCustomFish(ItemStack itemStack) {
        return (getCaughtFish(itemStack) != null);
    }


    private CaughtFish createCaughtFish(CustomFish fish, OfflinePlayer catcher) {
        double length;
        if (fish.getLengthMax() == fish.getLengthMin()) {
            length = fish.getLengthMax();
        } else {
            int min = (int)fish.getLengthMin();
            int max = (int)fish.getLengthMax();

            length = this.random.nextInt(max - min + 1) + min;
            length += 0.1D * this.random.nextInt(10);
        }

        return new CaughtFish(fish, length, catcher);
    }

    private Rarity getRandomRarity() {
        double currentVar = 0.0D;
        double randomVar = Math.random();

        for (Rarity rarity : this.rarityList) {
            currentVar += rarity.getChance();

            if (randomVar <= currentVar) {
                return rarity;
            }
        }

        return null;
    }

    private CustomFish getRandomFish(Rarity rarity, Player player) {
        List<CustomFish> list = new ArrayList<>(this.rarityMap.get(rarity));

        Iterator<CustomFish> it = list.iterator();
        while (it.hasNext()) {
            CustomFish fish = it.next();

            boolean remove = false;
            for (Condition condition : fish.getConditions()) {
                if (!condition.isSatisfying(player)) {
                    remove = true;
                }
            }

            if (remove) {
                it.remove();
            }
        }

        int index = this.random.nextInt(list.size());
        return list.get(index);
    }





    private String encodeFishData(CaughtFish fish) {
        String data = "|".concat("name:" + fish.getInternalName() + "|").concat("length:" + fish.getLength() + "|").concat("catcher:" + fish.getCatcher().getUniqueId()).replaceAll("", "§");
        data = data.substring(0, data.length() - 1);
        return data;
    }

    private CaughtFish decodeFishData(String displayName) {
        String[] split = displayName.replaceAll("§", "").split("\\|");
        if (split.length < 1) {
            return null;
        }

        String name = null;
        double length = 0.0D;
        OfflinePlayer catcher = null;

        for (int i = 1; i < split.length; i++) {
            String[] arr = split[i].split(":");
            if (arr.length < 2) {
                break;
            }

            String key = arr[0];
            String value = arr[1];

            switch (key) {
                case "name":
                    name = value;
                    break;
                case "length":
                    length = Double.parseDouble(value);
                    break;
                case "catcher":
                    catcher = this.plugin.getServer().getOfflinePlayer(UUID.fromString(value));
                    break;
            }

        }
        if (name == null) {
            return null;
        }

        CustomFish fish = getCustomFish(name);
        if (fish == null) {
            return null;
        }

        return new CaughtFish(fish, length, catcher);
    }
}