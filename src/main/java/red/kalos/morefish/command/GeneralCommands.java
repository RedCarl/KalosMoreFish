package red.kalos.morefish.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import red.kalos.morefish.MoreFish;
import red.kalos.morefish.manager.ContestManager;

import java.util.ArrayList;
import java.util.List;

public class GeneralCommands implements CommandExecutor, TabCompleter {
    private final MoreFish plugin;
    private final ContestManager contest;

    public GeneralCommands(MoreFish plugin) {
        this.plugin = plugin;
        this.contest = plugin.getContestManager();
    }

    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> list = new ArrayList<>();

        if (args.length < 2) {
            if (sender.hasPermission("morefish.admin")) {
                list.add("help");
                list.add("start");
                list.add("stop");
                list.add("clear");
                list.add("rewards");
            }

            if (sender.hasPermission("morefish.top")) {
                list.add("top");
            }

            if (sender.hasPermission("morefish.shop")) {
                list.add("shop");
            }
        }

        String finalArg = args[args.length - 1];
        list.removeIf(s -> !s.startsWith(finalArg));

        return list;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1 || "help".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("morefish.help")) {
                sender.sendMessage(this.plugin.getLocale().getString("no-permission"));
                return true;
            }

            String prefix = "§b[MoreFish]§r ";
            sender.sendMessage(prefix + "§3> ===== §b§lMoreFish §bv" + this.plugin.getDescription().getVersion() + "§3 ===== <");
            sender.sendMessage(prefix + "/" + label + " help");
            sender.sendMessage(prefix + "/" + label + " start [runningTime (sec)]");
            sender.sendMessage(prefix + "/" + label + " stop");
            sender.sendMessage(prefix + "/" + label + " rewards");
            sender.sendMessage(prefix + "/" + label + " clear");
            sender.sendMessage(prefix + "/" + label + " reload");
            sender.sendMessage(prefix + "/" + label + " top");
            sender.sendMessage(prefix + "/" + label + " shop [player]");

            return true;
        }  if ("start".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("morefish.admin")) {
                sender.sendMessage(this.plugin.getLocale().getString("no-permission"));
                return true;
            }

            if (this.contest.hasStarted()) {
                sender.sendMessage(this.plugin.getLocale().getString("already-ongoing"));
                return true;
            }

            boolean hasTimer = false;
            long sec = 0L;

            if (args.length > 1) {
                try {
                    sec = Long.parseLong(args[1]);
                } catch (NumberFormatException ex) {
                    sender.sendMessage(String.format(this.plugin.getLocale().getString("not-number"), args[1]));
                    return true;
                }

                if (sec <= 0L) {
                    sender.sendMessage(this.plugin.getLocale().getString("not-positive"));
                    return true;
                }

                hasTimer = true;
                this.contest.startWithTimer(sec);
            } else {
                this.contest.start();
            }


            String msg = this.plugin.getLocale().getString("contest-start");
            boolean broadcast = this.plugin.getConfig().getBoolean("messages.broadcast-start");

            if (broadcast) {
                this.plugin.getServer().broadcastMessage(msg);
            } else {
                sender.sendMessage(msg);
            }

            if (hasTimer) {


                String msgTimer = this.plugin.getLocale().getString("contest-start-timer").replaceAll("%sec%", Long.toString(sec)).replaceAll("%time%", this.plugin.getTimeString(sec));

                if (broadcast) {
                    this.plugin.getServer().broadcastMessage(msgTimer);
                } else {
                    sender.sendMessage(msgTimer);
                }
            }

            return true;
        }  if ("stop".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("morefish.admin")) {
                sender.sendMessage(this.plugin.getLocale().getString("no-permission"));
                return true;
            }

            if (!this.contest.hasStarted()) {
                sender.sendMessage(this.plugin.getLocale().getString("already-stopped"));
                return true;
            }


            String msg = this.plugin.getLocale().getString("contest-stop");
            boolean showRanking = this.plugin.getConfig().getBoolean("messages.show-top-on-ending");
            boolean broadcast = this.plugin.getConfig().getBoolean("messages.broadcast-stop");

            if (broadcast) {
                this.plugin.getServer().broadcastMessage(msg);
            } else {
                sender.sendMessage(msg);
            }

            if (showRanking) {
                sendRankingMessage(sender, broadcast);
            }


            this.contest.stop();

            return true;
        }  if ("clear".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("morefish.admin")) {
                sender.sendMessage(this.plugin.getLocale().getString("no-permission"));
                return true;
            }

            if (!this.contest.hasStarted()) {
                sender.sendMessage(this.plugin.getLocale().getString("not-ongoing"));
                return true;
            }

            this.contest.clearRecords();

            sender.sendMessage(this.plugin.getLocale().getString("clear-records"));

            return true;
        }  if ("reload".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("morefish.admin")) {
                sender.sendMessage(this.plugin.getLocale().getString("no-permission"));
                return true;
            }

            this.plugin.reloadConfig();
            boolean loaded = this.plugin.getLocale().loadFiles();

            if (!loaded) {
                sender.sendMessage(this.plugin.getLocale().getString("failed-to-reload"));
                return true;
            }

            this.plugin.getFishManager().loadFishList();
            this.plugin.loadFishShop();
            this.plugin.scheduleAutoRunning();

            sender.sendMessage(this.plugin.getLocale().getString("reload-config"));

            return true;
        }  if ("top".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("morefish.top")) {
                sender.sendMessage(this.plugin.getLocale().getString("no-permission"));
                return true;
            }

            if (!this.contest.hasStarted()) {
                sender.sendMessage(this.plugin.getLocale().getString("not-ongoing"));
                return true;
            }

            if (this.contest.getRecordAmount() < 1) {
                String msg = this.plugin.getLocale().getString("top-no-record");
                sender.sendMessage(msg);
            } else {
                sendRankingMessage(sender, false);
            }

            return true;
        }  if ("rewards".equalsIgnoreCase(args[0])) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(this.plugin.getLocale().getString("in-game-command"));
                return true;
            }

            Player player = (Player)sender;

            if (!player.hasPermission("morefish.admin")) {
                player.sendMessage(this.plugin.getLocale().getString("no-permission"));
                return true;
            }

            this.plugin.getRewardsGUI().openGUI(player);

            return true;
        }  if ("shop".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                if (!sender.hasPermission("morefish.shop")) {
                    sender.sendMessage(this.plugin.getLocale().getString("no-permission"));
                    return true;
                }

                if (!(sender instanceof Player)) {
                    sender.sendMessage(this.plugin.getLocale().getString("in-game-command"));
                    return true;
                }

                Player player = (Player)sender;

                if (this.plugin.getFishShopGUI() == null) {
                    sender.sendMessage(this.plugin.getLocale().getString("shop-disabled"));
                    return true;
                }

                this.plugin.getFishShopGUI().openGUI(player);
            } else {
                if (!sender.hasPermission("morefish.admin")) {
                    sender.sendMessage(this.plugin.getLocale().getString("no-permission"));
                    return true;
                }

                Player player = getPlayer(args[1]);

                if (player == null) {
                    sender.sendMessage(String.format(this.plugin.getLocale().getString("player-not-found"), args[1]));
                    return true;
                }

                this.plugin.getFishShopGUI().openGUI(player);
                sender.sendMessage(String.format(this.plugin.getLocale().getString("forced-player-to-shop"), player.getName()));
            }

            return true;
        }
        sender.sendMessage(this.plugin.getLocale().getString("invalid-command"));

        return true;
    }


    private Player getPlayer(String name) {
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) {
                return player;
            }
        }
        return null;
    }

    private void sendRankingMessage(CommandSender sender, boolean broadcast) {
        String format = this.plugin.getLocale().getString("top-list");
        int limit = this.plugin.getConfig().getInt("messages.top-number");

        for (int i = 1; i < limit + 1; i++) {
            ContestManager.Record record = this.contest.getRecord(i);

            if (record == null) {
                break;
            }




            String msg = format.replaceAll("%ordinal%", this.plugin.getOrdinal(i)).replaceAll("%number%", Integer.toString(i)).replaceAll("%player%", record.getPlayer().getName()).replaceAll("%length%", record.getLength() + "").replaceAll("%fish%", record.getFishName());

            if (broadcast) {
                this.plugin.getServer().broadcastMessage(msg);
            } else {
                sender.sendMessage(msg);
            }
        }

        if (broadcast) {
            for (Player player : this.plugin.getServer().getOnlinePlayers()) {
                sendPrivateRankingMessage(player);
            }
        }
        else if (sender instanceof Player) {
            Player player = (Player)sender;
            sendPrivateRankingMessage(player);
        }
    }



    private void sendPrivateRankingMessage(Player player) {
        String msg;
        if (this.plugin.getContestManager().hasRecord(player)) {
            int number = this.contest.getNumber(player);
            ContestManager.Record record = this.contest.getRecord(number);






            msg = this.plugin.getLocale().getString("top-mine").replaceAll("%ordinal%", this.plugin.getOrdinal(number)).replaceAll("%number%", Integer.toString(number)).replaceAll("%player%", record.getPlayer().getName()).replaceAll("%length%", record.getLength() + "").replaceAll("%fish%", record.getFishName());
        } else {
            msg = this.plugin.getLocale().getString("top-mine-no-record");
        }

        player.sendMessage(msg);
    }
}