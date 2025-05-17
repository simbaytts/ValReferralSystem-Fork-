package ru.valerchik.valreferralsystem;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ReferralManager {
    private final JavaPlugin plugin;
    private final CustomConfig customConfig;
    private final Map<UUID, UUID> referrals;
    private final int maxReferrals;
    private final int maxReferralTimes;

    public ReferralManager(JavaPlugin plugin, CustomConfig customConfig) {
        this.plugin = plugin;
        this.customConfig = customConfig;
        this.referrals = new HashMap<>();
        this.maxReferrals = plugin.getConfig().getInt("settings.maxReferrals", 5);
        this.maxReferralTimes = plugin.getConfig().getInt("settings.maxReferralTimes", 1);
    }

    public void loadReferrals() {
        referrals.clear();
        if (customConfig.getConfiguration().contains("referrals")) {
            Set<String> keys = customConfig.getConfiguration().getConfigurationSection("referrals").getKeys(false);
            for (String key : keys) {
                try {
                    UUID referralUUID = UUID.fromString(key);
                    String referrerStr = customConfig.getConfiguration().getString("referrals." + key);
                    UUID referrerUUID = UUID.fromString(referrerStr);
                    referrals.put(referralUUID, referrerUUID);
                } catch (Exception e) {
                    plugin.getLogger().warning("Некорректная запись реферала: " + key);
                }
            }
        }
        plugin.getLogger().info("Загружено рефералов: " + referrals.size());
    }

    public void saveReferrals() {
        customConfig.getConfiguration().set("referrals", null);
        for (Map.Entry<UUID, UUID> entry : referrals.entrySet()) {
            customConfig.getConfiguration().set("referrals." + entry.getKey().toString(), entry.getValue().toString());
        }
        customConfig.save();
        plugin.getLogger().info("Сохранено рефералов: " + referrals.size());
    }

    public boolean hasReferral(Player player) {
        return referrals.containsKey(player.getUniqueId());
    }

    public UUID getReferrer(Player player) {
        return referrals.get(player.getUniqueId());
    }

    public List<UUID> getReferralsOf(Player referrer) {
        List<UUID> list = new ArrayList<>();
        for (Map.Entry<UUID, UUID> entry : referrals.entrySet()) {
            if (entry.getValue().equals(referrer.getUniqueId())) {
                list.add(entry.getKey());
            }
        }
        return list;
    }

    public int getReferralCount(Player player) {
        return getReferralsOf(player).size();
    }

    public int getMaxReferrals() {
        return maxReferrals;
    }

    public int getMaxReferralTimes() {
        return maxReferralTimes;
    }

    public int getReferralTimes(Player player) {
        int count = 0;
        for (UUID uuid : referrals.keySet()) {
            if (uuid.equals(player.getUniqueId())) count++;
        }
        return count;
    }

    public boolean canBeReferred(Player player) {
        return getReferralTimes(player) < getMaxReferralTimes();
    }

    public boolean canRefer(Player referrer) {
        return getReferralCount(referrer) < getMaxReferrals();
    }

    public boolean addReferral(Player referral, Player referrer) {
        if (hasReferral(referral)) return false;
        if (!canBeReferred(referral)) return false;
        if (!canRefer(referrer)) return false;

        referrals.put(referral.getUniqueId(), referrer.getUniqueId());
        saveReferrals();

        giveReward(referrer, referral, plugin.getConfig().getStringList("rewards.referrer"));
        giveReward(referrer, referral, plugin.getConfig().getStringList("rewards.referral"));

        return true;
    }

    private String parsePlaceholders(String cmd, Player referrer, Player referral) {
        return cmd
                .replace("{referrer}", referrer.getName())
                .replace("{referral}", referral.getName())
                .replace("{referrer_uuid}", referrer.getUniqueId().toString())
                .replace("{referral_uuid}", referral.getUniqueId().toString())
                .replace("%referrer%", referrer.getName())
                .replace("%referral%", referral.getName())
                .replace("%referrer_uuid%", referrer.getUniqueId().toString())
                .replace("%referral_uuid%", referral.getUniqueId().toString());
    }

    private void giveReward(Player referrer, Player referral, List<String> commands) {
        for (String cmd : commands) {
            String parsed = parsePlaceholders(cmd, referrer, referral);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
        }
    }
}
