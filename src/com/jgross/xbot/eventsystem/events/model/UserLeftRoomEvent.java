package com.jgross.xbot.eventsystem.events.model;

import com.jgross.xbot.eventsystem.EventList;
import com.jgross.xbot.eventsystem.events.UserRoomEvent;
import com.jgross.xbot.model.ChatUser;
import com.jgross.xbot.model.ChatRoom;

public class UserLeftRoomEvent extends UserRoomEvent {

    private static final EventList events = new EventList();
    
    public UserLeftRoomEvent(ChatRoom chatRoom, ChatUser user, String nick) {
        super(chatRoom, user, nick);
    }

    @Override
    public EventList getEvents() {
        return events;
    }
    
    public static EventList getEventList() {
        return events;
    }

}
