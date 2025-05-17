package ru.valerchik.valreferralsystem;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RefReloadCommand implements CommandExecutor {

   private final ValReferralSystem plugin;
   private final ReferralManager referralManager;

   public RefReloadCommand(ValReferralSystem plugin, ReferralManager referralManager) {
      this.plugin = plugin;
      this.referralManager = referralManager;
   }

   @Override
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!sender.hasPermission("ref.reload")) {
         sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.noPermission")));
         return true;
      }
      plugin.reloadConfig();
      referralManager.loadReferrals();
      sender.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.pluginReloaded")));
      return true;
   }
}
