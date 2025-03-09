package de.emn4tor.commands.tpa;

/*
 *  @author: Emn4tor
 *  @created: 08.03.2025
 */

import de.emn4tor.CorvusTPA;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TPACommand implements CommandExecutor {
    private final CorvusTPA plugin;

    public TPACommand(CorvusTPA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }

        if (args.length < 1) {
            player.sendMessage("Usage: /tpa <player>");
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            plugin.getMessageManager().sendPlayerNotFound(player, targetName);
            return true;
        }

        if (target.equals(player)) {
            plugin.getMessageManager().sendSelfTeleport(player);
            return true;
        }

        if (plugin.getRequestManager().hasOutgoingRequest(player.getUniqueId())) {
            plugin.getMessageManager().sendAlreadyHasRequest(player);
            return true;
        }

        plugin.getRequestManager().createRequest(player, target, false);
        return true;
    }
}

