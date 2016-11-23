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
     * Get the hipchat user that left the room.
     * If no API Key is present in the bot, then this method may return null. The only time it will not return null is if
     * the room was created with an API Key
     * @return
     */
    public ChatUser getHipchatUser() {
        return user;
    }
    
    /**
     * Return the nickname of this user.
     * <b>Example</b>
     * "Bob Joe"
     * @return
     */
    public String getNickname() {
        return nick.split("\\/")[1];
    }
    
    /**
     * Return the JID of this user
     * <b>Example:</b>
     * "11111_111111@chat.hipchat.com"
     * @return
     */
    public String getJID() {
        return nick;
    }

}
