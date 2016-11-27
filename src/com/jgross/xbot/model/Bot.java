package com.jgross.xbot.model;

import com.jgross.xbot.networking.Connection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.util.List;

public interface Bot {
    
    /**
     * This method is called whenever a message is received
     */
    void receiveMessage(String message, String from, ChatRoom chatRoom);
    
    /**
     * This method is called after the bot connects and logs-in.
     * You can use this method to auto-join a room.
     */
    void onLoad();
    
    /**
     * Run this bot, this method should connect and login, and register any events.
     * @param connection
     */
    void run(Connection connection);
    
    /**
     * Send a message to the currently selected room. You can get the currently
     * selected room by calling {@link Bot#getSelectedRoom()} and change the currently
     * selected room by calling {@link Bot#changeRoom(ChatRoom)}
     * @param message The body of the message to send
     */
    void sendMessage(String message);
    
    /**
     * Send a message to the ChatRoom provided. You can also send the message by calling
     * {@link ChatRoom#sendMessage(String, String)}
     * @param message The body of the message to send
     * @param chatRoom The ChatRoom object to send this message to.
     */
    void sendMessage(String message, ChatRoom chatRoom);
    
    /**
     * Change the currently selected ChatRoom. You can get the currently selected ChatRoom by
     * calling {@link Bot#getSelectedRoom()}
     * @param chatRoom The ChatRoom object to change to
     */
    void changeRoom(ChatRoom chatRoom);
    
    /**
     * @return The ChatRoom object that is currently selected
     */
    ChatRoom getSelectedRoom();
    
    /**
     * @return an unmodifiable list of {@link ChatUser}'s. These users may be offline, online, or may be deleted.
     */
    List<ChatUser> getUsers();
    
    /**
     * @return username this bot will login into the server with.
     */
    String username();
    
    /**
     * @return the nickname the bot will join rooms with.
     */
    String nickname();
    
    /**
     * @return password this bot will use to connect.
     */
    String password();

    XMPPTCPConnectionConfiguration connectionConfig();

    /**
     * @return the connection object that is handling the XMPP API for this bot.
     */
    Connection getConnection();
}
