package com.oheers.fish.economy;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.economy.EconomyType;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.messages.EMFSingleMessage;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.coinsengine.api.currency.Currency;

import java.util.logging.Level;

public class CoinsEngineEconomyType implements EconomyType {

    private @Nullable Currency economy;

    public CoinsEngineEconomyType() {
        EvenMoreFish emf = EvenMoreFish.getInstance();
        emf.getLogger().log(Level.INFO, "Economy attempting to hook into CoinsEngine.");
        if (Bukkit.getPluginManager().isPluginEnabled("CoinsEngine")) {
            economy = CoinsEngineAPI.getCurrency(EvenMoreFish.getInstance().getConfig().getString("economy.coinsengine.currency"));
            emf.getLogger().log(Level.INFO, "Economy hooked into CoinsEngine.");
        }
    }

    @Override
    public String getIdentifier() {
        return "CoinsEngine";
    }

    @Override
    public double getMultiplier() {
        return MainConfig.getInstance().getEconomyMultiplier(this);
    }

    @Override
    public boolean deposit(@NotNull OfflinePlayer player, double amount, boolean allowMultiplier) {
        if(!isAvailable()) {
            return false;
        }

        return CoinsEngineAPI.addBalance(player.getUniqueId(), economy, prepareValue(amount, allowMultiplier));
    }

    @Override
    public boolean withdraw(@NotNull OfflinePlayer player, double amount, boolean allowMultiplier) {
        if(!isAvailable()) {
            return false;
        }

        return CoinsEngineAPI.removeBalance(player.getUniqueId(), economy, prepareValue(amount, allowMultiplier));
    }

    @Override
    public boolean has(@NotNull OfflinePlayer player, double amount) {
        if (!isAvailable()) {
            return false;
        }
        return get(player) >= amount;
    }

    @Override
    public double get(@NotNull OfflinePlayer player) {
        if (!isAvailable()) {
            return 0;
        }
        return CoinsEngineAPI.getBalance(player.getUniqueId(), economy);
    }

    @Override
    public double prepareValue(double value, boolean applyMultiplier) {
        double finalValue = value;
        if (applyMultiplier) {
            finalValue = value * getMultiplier();
        }
        return finalValue;
    }

    @Override
    public @Nullable Component formatWorth(double totalWorth, boolean applyMultiplier) {
        if (!isAvailable()) {
            return null;
        }
        int worth = (int) prepareValue(totalWorth, applyMultiplier);
        String display = MainConfig.getInstance().getEconomyDisplay(this);
        if (display == null) {
            display = "{amount} Money";
        }
        EMFSingleMessage message = EMFSingleMessage.fromString(display);
        message.setVariable("{amount}", String.valueOf(worth));
        return message.getComponentMessage();
    }

    @Override
    public boolean isAvailable() {
        return (MainConfig.getInstance().isEconomyEnabled(this) && economy != null);
    }
}
