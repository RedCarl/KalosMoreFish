package red.kalos.morefish.hooker;

import red.kalos.morefish.MoreFish;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHooker {
    private final MoreFish plugin;
    private Economy econ = null;

    public VaultHooker(MoreFish plugin) {
        this.plugin = plugin;
    }

    public boolean setupEconomy() {
        if (this.plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = this.plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        this.econ = rsp.getProvider();
        return (this.econ != null);
    }

    public Economy getEconomy() {
        return this.econ;
    }
}