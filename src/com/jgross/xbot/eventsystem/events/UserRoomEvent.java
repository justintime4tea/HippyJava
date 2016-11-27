package com.jgross.xbot.eventsystem.events;

import com.jgross.xbot.model.ChatRoom;
import com.jgross.xbot.model.ChatUser;

public abstract class UserRoomEvent extends RoomEvent {

    private ChatUser user;
    private String nick;
    
    public UserRoomEvent(ChatRoom chatRoom, ChatUser user, String nick) {
        super(chatRoom);
        this.nick = nick;
        this.user = user;
    }
    
    /**
     * @return the chat user that left the room.
     */
    public ChatUser getChatUser() {
        return user;
    }
    
    /**
     * @return the nickname of this user.
     */
    public String getNickname() {
        return nick.split("\\/")[1];
    }
    
    /**
     * @return the JID of this user
     */
    public String getJID() {
        return nick;
    }

}
