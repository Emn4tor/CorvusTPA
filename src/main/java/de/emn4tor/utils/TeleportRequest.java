package de.emn4tor.utils;

/*
 *  @author: Emn4tor
 *  @created: 08.03.2025
 */

import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class TeleportRequest {
    private final UUID senderUUID;
    private final UUID targetUUID;
    private final boolean here;
    private BukkitTask task;

    public TeleportRequest(UUID senderUUID, UUID targetUUID, boolean here) {
        this.senderUUID = senderUUID;
        this.targetUUID = targetUUID;
        this.here = here;
    }

    public UUID getSenderUUID() {
        return senderUUID;
    }

    public UUID getTargetUUID() {
        return targetUUID;
    }

    public boolean isHere() {
        return here;
    }

    public BukkitTask getTask() {
        return task;
    }

    public void setTask(BukkitTask task) {
        this.task = task;
    }
}