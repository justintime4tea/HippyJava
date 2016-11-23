package com.jgross.xbot.eventsystem.events;

import com.jgross.xbot.eventsystem.Event;
import com.jgross.xbot.model.Room;

public abstract class RoomEvent extends Event {
    
    private Room room;
    
    public RoomEvent(Room room) {
        this.room = room;
    }
    
    /**
     * Get the room that this event is associated with.
     * @return
     */
    public Room getRoom() {
        return room;
    }

}
