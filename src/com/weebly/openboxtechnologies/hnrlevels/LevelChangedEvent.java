package com.weebly.openboxtechnologies.hnrlevels;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public final class LevelChangedEvent extends org.bukkit.event.Event{

    private static final HandlerList handlers = new HandlerList();

    private UUID id;

    public LevelChangedEvent(UUID player) {
        id = player;
    }

    public UUID getPlayer() {
        return id;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
