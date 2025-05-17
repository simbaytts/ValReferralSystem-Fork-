package ru.valerchik.valreferralsystem;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RefCommand implements CommandExecutor {

   private final ValReferralSystem plugin;
   private final ReferralManager referralManager;

   public RefCommand(ValReferralSystem plugin, ReferralManager referralManager) {
      this.plugin = plugin;
      this.referralManager = referralManager;
   }

   @Override
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage("Только игроки могут использовать эту команду.");
         return true;
      }
      Player player = (Player) sender;

      if (!player.hasPermission("ref.use")) {
         player.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.noPermission")));
         return true;
      }

      if (args.length == 0) {
         player.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.invalidUsage")));
         return true;
      }

      String sub = args[0].toLowerCase();

      switch (sub) {
         case "look": {
            if (args.length != 2) {
               player.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.invalidUsage")));
               return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
               player.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.playerNotFound")));
               return true;
            }
            if (!target.hasPermission("ref.ref")) {
               player.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.noReferralPermission")));
               return true;
            }
            int count = referralManager.getReferralCount(target);
            player.sendMessage(plugin.colorize(
                    plugin.getConfig().getString("messages.referralCount")
                            .replace("{player}", target.getName())
                            .replace("{count}", String.valueOf(count))
            ));
            return true;
         }

         case "list": {
            List<UUID> refsUUIDs = referralManager.getReferralsOf(player);
            if (refsUUIDs.isEmpty()) {
               player.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.noReferrals")));
            } else {
               String names = refsUUIDs.stream()
                       .map(uuid -> {
                          Player p = Bukkit.getPlayer(uuid);
                          return p != null ? p.getName() : uuid.toString();
                       })
                       .collect(Collectors.joining(", "));
               player.sendMessage(plugin.colorize(
                       plugin.getConfig().getString("messages.yourReferrals")
                               .replace("{referrals}", names)
               ));
            }
            return true;
         }

         case "info": {
            UUID referrerUUID = referralManager.getReferrer(player);
            if (referrerUUID == null) {
               player.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.noReferrer")));
            } else {
               Player referrer = Bukkit.getPlayer(referrerUUID);
               String name = (referrer != null) ? referrer.getName() : referrerUUID.toString();
               player.sendMessage(plugin.colorize(
                       plugin.getConfig().getString("messages.yourReferrer")
                               .replace("{referrer}", name)
               ));
            }
            return true;
         }

         default: {
            Player referrerPlayer = Bukkit.getPlayer(args[0]);
            if (referrerPlayer == null || !referrerPlayer.hasPermission("ref.ref")) {
               player.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.invalidReferrer")));
               return true;
            }

            if (referrerPlayer.equals(player)) {
               player.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.cannotReferSelf")));
               return true;
            }

            if (referralManager.hasReferral(player)) {
               player.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.alreadyReferred")));
               return true;
            }

            if (!referralManager.canBeReferred(player)) {
               player.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.maxReferralTimesReached")));
               return true;
            }

            if (!referralManager.canRefer(referrerPlayer)) {
               player.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.maxReferralsReached")));
               return true;
            }

            if (referralManager.addReferral(player, referrerPlayer)) {
               player.sendMessage(plugin.colorize(
                       plugin.getConfig().getString("messages.referralSuccess")
                               .replace("{referrer}", referrerPlayer.getName()))
               );
            } else {
               player.sendMessage(plugin.colorize(plugin.getConfig().getString("messages.referralFailed")));
            }
            return true;
         }
      }
   }
}
