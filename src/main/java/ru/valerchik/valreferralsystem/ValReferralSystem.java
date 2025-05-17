package ru.valerchik.valreferralsystem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ValReferralSystem extends JavaPlugin {

   private static ValReferralSystem instance;
   private ReferralManager referralManager;
   private CustomConfig customConfig;

   @Override
   public void onEnable() {
      instance = this;

      saveDefaultConfig();
      File referralsFile = new File(getDataFolder(), "referrals.yml");
      customConfig = new CustomConfig(referralsFile);

      referralManager = new ReferralManager(this, customConfig);
      referralManager.loadReferrals();

      PluginCommand refCommand = getCommand("ref");
      if (refCommand != null) {
         refCommand.setExecutor(new RefCommand(this, referralManager));
         refCommand.setTabCompleter(new RefTabCompleter(this));
      }

      PluginCommand reloadCommand = getCommand("refreload");
      if (reloadCommand != null) {
         reloadCommand.setExecutor(new RefReloadCommand(this, referralManager));
      }

      getLogger().info("ValReferralSystem enabled!");
      getLogger().info("ФОРК ОТ @guard_spigot / SIMBAY");
   }

   @Override
   public void onDisable() {
      if (referralManager != null) {
         referralManager.saveReferrals();
      }
      getLogger().info("ValReferralSystem disabled!");
      getLogger().info("ФОРК ОТ @guard_spigot / SIMBAY");
   }

   public static ValReferralSystem getInstance() {
      return instance;
   }

   public String colorize(String message) {
      return ChatColor.translateAlternateColorCodes('&', message);
   }
}
