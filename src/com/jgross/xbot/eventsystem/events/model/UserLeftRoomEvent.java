package com.jgross.xbot.eventsystem.events.model;

import com.jgross.xbot.eventsystem.EventList;
import com.jgross.xbot.eventsystem.events.UserRoomEvent;
import com.jgross.xbot.model.HipchatUser;
import com.jgross.xbot.model.Room;

public class UserLeftRoomEvent extends UserRoomEvent {

    private static final EventList events = new EventList();
    
    public UserLeftRoomEvent(Room room, HipchatUser user, String nick) {
        super(room, user, nick);
    }

    @Override
    public EventList getEvents() {
        return events;
    }
    
    public static EventList getEventList() {
        return events;
    }

}
