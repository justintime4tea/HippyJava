package com.jgross.xbot.eventsystem.events;

import com.jgross.xbot.eventsystem.Event;
import com.jgross.xbot.model.ChatRoom;

public abstract class RoomEvent extends Event {
    
    private ChatRoom chatRoom;
    
    public RoomEvent(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }
    
    /**
     * Get the chatRoom that this event is associated with.
     * @return
     */
    public ChatRoom getChatRoom() {
        return chatRoom;
    }

}
