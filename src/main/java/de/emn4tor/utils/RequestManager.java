package de.emn4tor.utils;

/*
 *  @author: Emn4tor
 *  @created: 08.03.2025
 */

import de.emn4tor.CorvusTPA;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RequestManager {
    private final CorvusTPA plugin;
    private final Map<UUID, TeleportRequest> requests = new HashMap<>();

    public RequestManager(CorvusTPA plugin) {
        this.plugin = plugin;
    }

    public void createRequest(Player sender, Player target, boolean here) {
        // Cancel any existing requests from this sender
        cancelRequest(sender.getUniqueId());

        // Create new request
        TeleportRequest request = new TeleportRequest(sender.getUniqueId(), target.getUniqueId(), here);
        requests.put(sender.getUniqueId(), request);

        // Send messages
        if (here) {
            plugin.getMessageManager().sendTpHereRequest(sender, target);
        } else {
            plugin.getMessageManager().sendTpaRequest(sender, target);
        }

        // Schedule request to expire
        int timeout = plugin.getConfig().getInt("request-timeout", 60);
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (requests.containsKey(sender.getUniqueId())) {
                requests.remove(sender.getUniqueId());
                Player senderPlayer = Bukkit.getPlayer(sender.getUniqueId());
                Player targetPlayer = Bukkit.getPlayer(target.getUniqueId());

                if (senderPlayer != null) {
                    plugin.getMessageManager().sendRequestExpired(senderPlayer);
                }

                if (targetPlayer != null) {
                    plugin.getMessageManager().sendRequestExpiredTarget(targetPlayer, senderPlayer);
                }
            }
        }, timeout * 20L); // Convert seconds to ticks

        request.setTask(task);
    }

    public TeleportRequest getRequest(UUID targetUUID) {
        for (TeleportRequest request : requests.values()) {
            if (request.getTargetUUID().equals(targetUUID)) {
                return request;
            }
        }
        return null;
    }

    public void acceptRequest(UUID targetUUID) {
        TeleportRequest request = getRequest(targetUUID);
        if (request != null) {
            Player sender = Bukkit.getPlayer(request.getSenderUUID());
            Player target = Bukkit.getPlayer(targetUUID);

            if (sender != null && target != null) {
                // Cancel the timeout task
                if (request.getTask() != null) {
                    request.getTask().cancel();
                }

                // Perform teleportation
                if (request.isHere()) {
                    // tphere: target teleports to sender
                    target.teleport(sender.getLocation());
                } else {
                    // tpa: sender teleports to target
                    sender.teleport(target.getLocation());
                }

                // Send messages
                plugin.getMessageManager().sendRequestAccepted(sender, target);

                // Remove the request
                requests.remove(request.getSenderUUID());
            }
        }
    }

    public void denyRequest(UUID targetUUID) {
        TeleportRequest request = getRequest(targetUUID);
        if (request != null) {
            Player sender = Bukkit.getPlayer(request.getSenderUUID());
            Player target = Bukkit.getPlayer(targetUUID);

            if (sender != null && target != null) {
                // Cancel the timeout task
                if (request.getTask() != null) {
                    request.getTask().cancel();
                }

                // Send messages
                plugin.getMessageManager().sendRequestDenied(sender, target);

                // Remove the request
                requests.remove(request.getSenderUUID());
            }
        }
    }

    public void cancelRequest(UUID senderUUID) {
        TeleportRequest request = requests.get(senderUUID);
        if (request != null) {
            // Cancel the timeout task
            if (request.getTask() != null) {
                request.getTask().cancel();
            }

            Player sender = Bukkit.getPlayer(senderUUID);
            Player target = Bukkit.getPlayer(request.getTargetUUID());

            if (sender != null) {
                plugin.getMessageManager().sendRequestCancelled(sender);
            }

            if (target != null && sender != null) {
                plugin.getMessageManager().sendRequestCancelledTarget(target, sender);
            }

            // Remove the request
            requests.remove(senderUUID);
        }
    }

    public void cancelAllRequests() {
        for (TeleportRequest request : requests.values()) {
            if (request.getTask() != null) {
                request.getTask().cancel();
            }
        }
        requests.clear();
    }

    public boolean hasOutgoingRequest(UUID senderUUID) {
        return requests.containsKey(senderUUID);
    }

    public boolean hasIncomingRequest(UUID targetUUID) {
        return getRequest(targetUUID) != null;
    }
}

