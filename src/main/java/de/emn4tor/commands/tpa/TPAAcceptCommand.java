package de.emn4tor.commands.tpa;

/*
 *  @author: Emn4tor
 *  @created: 08.03.2025
 */

import de.emn4tor.CorvusTPA;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TPAAcceptCommand implements CommandExecutor {
    private final CorvusTPA plugin;

    public TPAAcceptCommand(CorvusTPA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }

        if (!plugin.getRequestManager().hasIncomingRequest(player.getUniqueId())) {
            plugin.getMessageManager().sendNoRequestFound(player);
            return true;
        }

        plugin.getRequestManager().acceptRequest(player.getUniqueId());
        return true;
    }
}

