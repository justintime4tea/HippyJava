package com.jgross.xbot.eventsystem.events;

import com.jgross.xbot.eventsystem.Event;
import com.jgross.xbot.model.ChatRoom;

public abstract class RoomEvent extends Event {
    
    private ChatRoom chatRoom;
    
    public RoomEvent(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }
    
    /**
     * @return the chatRoom that this event is associated with.
     */
    public ChatRoom getChatRoom() {
        return chatRoom;
    }

}
