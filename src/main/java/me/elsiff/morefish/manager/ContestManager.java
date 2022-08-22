package me.elsiff.morefish.manager;
import java.io.File;
import java.io.IOException;
import java.util.*;

import me.elsiff.morefish.CaughtFish;
import me.elsiff.morefish.MoreFish;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ContestManager {
    private final RecordComparator comparator = new RecordComparator();
    private final MoreFish plugin;
    private final List<Record> recordList = new ArrayList<>();
    private final File fileRewards;
    private final FileConfiguration configRewards;
    private File fileRecords;
    private FileConfiguration configRecords;
    private boolean hasStarted = false;
    private TimerTask task = null;

    public ContestManager(MoreFish plugin) {
        this.plugin = plugin;

        if (plugin.getConfig().getBoolean("general.auto-start")) {
            this.hasStarted = true;
        }

        this.fileRewards = new File(plugin.getDataFolder(), "rewards.yml");

        createFile(this.fileRewards);
        this.configRewards = YamlConfiguration.loadConfiguration(this.fileRewards);

        if (plugin.getConfig().getBoolean("general.save-records")) {
            this.fileRecords = new File(plugin.getDataFolder(), "records.yml");
            createFile(this.fileRecords);
            this.configRecords = YamlConfiguration.loadConfiguration(this.fileRecords);

            loadRecords();
        }
    }

    private void createFile(File file) {
        if (!file.exists()) {
            try {
                boolean created = file.createNewFile();

                if (!created) {
                    this.plugin.getLogger().warning("Failed to create " + file.getName() + "!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveRewards() {
        try {
            this.configRewards.save(this.fileRewards);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadRecords() {
        this.recordList.clear();

        for (String path : this.configRecords.getKeys(false)) {
            UUID id = UUID.fromString(this.configRecords.getString(path + ".player"));
            String fishName = this.configRecords.getString(path + ".fish-name");
            double length = this.configRecords.getDouble(path + ".length");

            this.recordList.add(new Record(id, fishName, length));
        }

        this.recordList.sort(this.comparator);
    }

    public void saveRecords() {
        for (String path : this.configRecords.getKeys(false)) {
            this.configRecords.set(path, null);
        }

        for (int i = 0; i < this.recordList.size(); i++) {
            Record record = this.recordList.get(i);
            this.configRecords.set(i + ".player", record.getPlayer().getUniqueId().toString());
            this.configRecords.set(i + ".fish-name", record.getFishName());
            this.configRecords.set(i + ".length", record.getLength());
        }

        try {
            this.configRecords.save(this.fileRecords);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hasStarted() {
        return this.hasStarted;
    }

    public boolean hasTimer() {
        return (this.task != null);
    }

    public void start() {
        this.hasStarted = true;
    }

    public void startWithTimer(long sec) {
        this.task = new TimerTask(sec);
        this.task.runTaskTimer(this.plugin, 20L, 20L);

        if (this.plugin.hasBossBar()) {
            this.plugin.getBossBarManager().createTimerBar(sec);
        }

        start();
    }

    public void stop() {
        if (this.task != null) {
            if (this.plugin.hasBossBar()) {
                this.plugin.getBossBarManager().removeTimerBar();
            }

            this.task.cancel();
            this.task = null;
        }

        giveRewards();

        if (!this.plugin.getConfig().getBoolean("general.save-records")) {
            this.recordList.clear();
        }

        this.hasStarted = false;
    }

    private void giveRewards() {
        Set<Integer> receivers = new HashSet<>();

        ItemStack[] rewards = getRewards(); int i;
        for (i = 0; i < rewards.length - 1 && i < this.recordList.size(); i++) {
            ItemStack stack = rewards[i];

            if (stack != null && stack.getType() != Material.AIR) {


                OfflinePlayer player = getRecord(i + 1).getPlayer();
                sendReward(player, stack);

                receivers.add(i);
            }
        }
        if (this.plugin.hasEconomy()) {
            double[] cashPrizes = getCashPrizes(); int j;
            for (j = 0; j < cashPrizes.length - 1 && j < this.recordList.size(); j++) {
                double amount = cashPrizes[j];

                if (amount > 0.0D) {


                    OfflinePlayer player = getRecord(j + 1).getPlayer();
                    sendCashPrize(player, amount);

                    receivers.add(j);
                }
            }
            if (cashPrizes[7] > 0.0D) {
                for (j = 0; j < getRecordAmount(); j++) {
                    if (!receivers.contains(j)) {


                        Record record = getRecord(j + 1);

                        sendCashPrize(record.getPlayer(), cashPrizes[7]);
                    }
                }
            }
        }
        if (rewards[7] != null)
            for (i = 0; i < getRecordAmount(); i++) {
                if (!receivers.contains(i)) {


                    Record record = getRecord(i + 1);
                    sendReward(record.getPlayer(), rewards[7]);
                }
            }
    }

    private void sendReward(OfflinePlayer oPlayer, ItemStack stack) {
        if (!oPlayer.isOnline()) {
            this.plugin.getLogger().info(oPlayer.getName() + "'s reward of fishing contest has not been sent as the player is offline now.");

            return;
        }
        Player player = oPlayer.getPlayer();

        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(stack);
        } else {
            player.getWorld().dropItem(player.getLocation(), stack);
        }

        int number = getNumber(player);
        String msg = this.plugin.getLocale().getString("reward");




        msg = msg.replaceAll("%player%", player.getName()).replaceAll("%item%", getItemName(stack)).replaceAll("%ordinal%", this.plugin.getOrdinal(number)).replaceAll("%number%", Integer.toString(number));

        player.sendMessage(msg);
    }

    private void sendCashPrize(OfflinePlayer player, double amount) {
        if (!this.plugin.getVaultHooker().getEconomy().hasAccount(player)) {
            this.plugin.getLogger().info(player.getName() + "'s reward of fishing contest has not been sent as having no economy account.");
            return;
        }
        this.plugin.getVaultHooker().getEconomy().depositPlayer(player, amount);


        if (player.isOnline()) {
            int number = getNumber(player);
            String msg = this.plugin.getLocale().getString("reward-cash-prize");




            msg = msg.replaceAll("%player%", player.getName()).replaceAll("%amount%", Double.toString(amount)).replaceAll("%ordinal%", this.plugin.getOrdinal(number)).replaceAll("%number%", Integer.toString(number));

            player.getPlayer().sendMessage(msg);
        }
    }

    private String getItemName(ItemStack item) {
        return (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) ? item
                .getItemMeta().getDisplayName() : item.getType().name().toLowerCase().replaceAll("_", " ");
    }

    public ItemStack[] getRewards() {
        ItemStack[] rewards = new ItemStack[8];

        for (String path : this.configRewards.getKeys(false)) {
            if (!path.startsWith("reward_")) {
                continue;
            }
            int i = Integer.parseInt(path.substring(7));
            ItemStack item = this.configRewards.getItemStack("reward_" + i);

            rewards[i] = item;
        }

        return rewards;
    }

    public void setRewards(ItemStack[] rewards) {
        for (int i = 0; i < rewards.length; i++) {
            this.configRewards.set("reward_" + i, rewards[i]);
        }

        saveRewards();
    }

    public double[] getCashPrizes() {
        double[] arr = new double[8];

        for (String path : this.configRewards.getKeys(false)) {
            if (!path.startsWith("cash-prize_")) {
                continue;
            }
            int i = Integer.parseInt(path.substring(11));
            double amount = this.configRewards.getDouble("cash-prize_" + i);

            arr[i] = amount;
        }

        return arr;
    }

    public void setCashPrizes(double[] arr) {
        for (int i = 0; i < arr.length; i++) {
            this.configRewards.set("cash-prize_" + i, arr[i]);
        }

        saveRewards();
    }

    public boolean isNew1st(CaughtFish fish) {
        Record record = getRecord(1);

        return (record == null || record.getLength() < fish.getLength());
    }

    public void addRecord(OfflinePlayer player, CaughtFish fish) {
        ListIterator<Record> it = this.recordList.listIterator();
        while (it.hasNext()) {
            Record record = it.next();

            if (record.getPlayer().equals(player)) {
                if (record.getLength() < fish.getLength()) {
                    it.remove();

                    break;
                }

                return;
            }
        }
        this.recordList.add(new Record(player.getUniqueId(), fish));

        this.recordList.sort(this.comparator);
    }

    public Record getRecord(int number) {
        return (this.recordList.size() >= number) ? this.recordList.get(number - 1) : null;
    }

    public int getRecordAmount() {
        return this.recordList.size();
    }

    public boolean hasRecord(OfflinePlayer player) {
        for (Record record : this.recordList) {
            if (record.getPlayer().equals(player)) {
                return true;
            }
        }

        return false;
    }

    public double getRecordLength(OfflinePlayer player) {
        for (Record record : this.recordList) {
            if (record.getPlayer().equals(player)) {
                return record.getLength();
            }
        }

        return 0.0D;
    }

    public int getNumber(OfflinePlayer player) {
        for (int i = 0; i < this.recordList.size(); i++) {
            if (this.recordList.get(i).getPlayer().getUniqueId().equals(player.getUniqueId())) {
                return i + 1;
            }
        }

        return 0;
    }

    public void clearRecords() {
        this.recordList.clear();
    }

    private class RecordComparator implements Comparator<Record> { private RecordComparator() {}

        public int compare(Record arg0, Record arg1) {
            return Double.compare(arg1.getLength(), arg0.getLength());
        } }


    public class Record {
        private final UUID id;
        private final String fishName;
        private final double length;

        public Record(UUID id, CaughtFish fish) {
            this(id, fish.getName(), fish.getLength());
        }

        public Record(UUID id, String fishName, double length) {
            this.id = id;
            this.fishName = fishName;
            this.length = length;
        }

        public OfflinePlayer getPlayer() {
            return ContestManager.this.plugin.getServer().getOfflinePlayer(this.id);
        }

        public String getFishName() {
            return this.fishName;
        }

        public double getLength() {
            return this.length;
        }
    }

    private class TimerTask extends BukkitRunnable {
        private final long timer;
        private long passed = 0L;

        public TimerTask(long sec) {
            this.timer = sec;
        }

        public void run() {
            this.passed++;

            if (ContestManager.this.plugin.hasBossBar()) {
                ContestManager.this.plugin.getBossBarManager().updateTimerBar(this.passed, this.timer);
            }

            if (this.passed >= this.timer) {
                ContestManager.this.plugin.getServer().dispatchCommand(ContestManager.this.plugin.getServer().getConsoleSender(), "morefish stop");
                cancel();
            }
        }
    }
}