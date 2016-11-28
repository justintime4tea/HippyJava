package com.jgross.xbot.model;

import com.jgross.xbot.XBotLib;
import com.jgross.xbot.eventsystem.EventHandler;
import com.jgross.xbot.eventsystem.Listener;
import com.jgross.xbot.eventsystem.events.model.MessageReceivedEvent;
import com.jgross.xbot.networking.Connection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.roster.RosterEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class XBot implements Bot, Listener {

    private Connection connection;
    private ChatRoom chatRoom;

    @Override
    public void run(Connection connection) {
        XBotLib.events.registerEvents(new Listener() {
            @SuppressWarnings("unused")
            @EventHandler
            public void messageEvent(MessageReceivedEvent event) {
                receiveMessage(event.body(), event.from(), event.getChatRoom());
            }
        });
        this.connection = connection;
        try {
            connection.connect();
            connection.login(username(), password());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        onLoad();
    }

    @Override
    public void sendMessage(String message) {
        if (chatRoom == null)
            return;
        sendMessage(message, chatRoom);
    }

    /**
     * Change the currently chatRoom room. You must be in the room in order to change to it, to join a room, invoke the method
     * {@link XBot#joinRoom(String)}.
     * The room name provide must be the JID name of the room. If {@link XBot#xmppHost()} returns a valid
     * API Key, then the room name provided can be the normal name of the room.
     * @param name
     */
    public void changeRoom(String name) {
        if (findRoom(name) != null)
            changeRoom(findRoom(name));
    }
    
    /**
     * Join a room with the given name.
     * The room name provided must be the JID name of the room. If {@link XBot#xmppHost()} returns a valid API Key, then
     * the room name provided can be the normal name of the room.
     * @param roomName
     * @return
     *        Returns whether the operation was successful or not.
     */
    public boolean joinRoom(String roomName) throws XMPPException.XMPPErrorException, SmackException {
        connection.joinRoom(xmppHost(), roomName, nickname());
        chatRoom = findRoom(roomName);
        return true;
    }


    @Override
    public ChatRoom getSelectedRoom() {
        return chatRoom;
    }

    @Override
    public void changeRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    /**
     * Send a message a room. You must be connected to the room to send this message, if your not connected to the room, then please
     * invoke the method {@link XBot#joinRoom(String)}
     * @param name
     *            The name of the room.
     * @param message
     *               The message to send
     */
    public void sendMessageToRoom(String name, String message) {
        if (findRoom(name) != null)
            sendMessage(message, findRoom(name));
    }
    
    /**
     * Send a private message to someone. The parameter "to" must be the JID URL of the user, you can convert a nick such as
     * "Bob Joe" to a JID URL by invoking the method {@link XBot#nickToJID(String)} and using the String returned as the
     * JID URL.
     * @param message
     *              The message to send.
     * @param to
     *          The JID of the user to send this message to.
     * @return
     *        Whether this operation was successful or not.
     */
    public boolean sendPM(String message, String to) {
        if (!to.contains("@")) { //oh noes its not a JID! The user didnt follow the rules!
            ChatUser user = findUser(to);
            if (user != null)
                to = nickToJID(user.getName());
            else //Ok I just dont know anymore
                return false;
        }
        try {
            getConnection().sendPM(message, to);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    /**
     * Send a private message to someone.
     * @param message
     *              The message to send.
     * @param user
     *          The user to send the message to.
     * @return
     *        Whether this operation was successful or not.
     */
    public boolean sendPM(String message, ChatUser user) {
        return sendPM(message, nickToJID(user.getName()));
    }
    
    /**
     * Convert a hipchat nick to its JID equal. </br>
     * <b>For Example:</b> </br>
     * Pass "Bob Joe" as a parameter and this method will return something like "11111_111111@chat.hipchat.com".
     * If the method can't find the JID for the nick given, then the given nick will be returned.
     * @param nick
     *            The nick to convert
     * @return
     *        The JID for the nick
     */
    public String nickToJID(String nick) {
        for (RosterEntry r : getConnection().getRoster().getEntries()) {
            if (r.getName().equals(nick))
                return r.getUser();
        }
        return nick;
    }

    @Override
    public void sendMessage(String message, ChatRoom chatRoom) {
        chatRoom.sendMessage(message, nickname());
    }

    /**
     * Get an unmodifiable list of {@link ChatUser}'s. These users may be offline, online, or may be deleted. </br>
     * In order for this method to work properly, the {@link XBot#xmppHost()} method must return a valid XMPP host,
     * otherwise this method will return an empty list.
     * @return
     */
    @Override
    public List<ChatUser> getUsers() {
        ArrayList<ChatUser> users = new ArrayList<>();
        if (xmppHost().equals(""))
            return Collections.unmodifiableList(users);
        //TODO: Get list of occupants of joined rooms and roster
//        ChatUser[] chatUsers = ChatUser.getChatUsers(xmppHost());
//        for (ChatUser user : chatUsers) {
//            users.add(user);
//        }
        return Collections.unmodifiableList(users);
    }
    
    /**
     * Find a ChatUser by providing a name.
     * This wont check for part of the name and this search is also case sensitive. So "Bob Joe" and "bob joe" will return different results.
     * @param name
     *            The name to search for.
     * @return
     *        The ChatUser object or null if none is found.
     */
    public ChatUser findUser(String name) {
        for (ChatUser u : getUsers()) {
            if (u.getName().equals(name))
                return u;
        }
        return null;
    }

    /**
     * Look for a room that you are currently connected to
     * @param name
     *            The name of the room to look for
     * @return
     *        The room
     */
    public ChatRoom findRoom(String name) {
        ChatRoom r = connection.findConnectedRoom(name);
        if (r == null) {
            for (ChatRoom chatRoom : connection.getRooms()) {
                if (chatRoom.getTrueName(xmppHost()).equals(name))
                    return chatRoom;
            }
            return null;
        }
        else
            return r;
    }

    /**
     * Get a list of rooms you are currently connected to.
     * @return
     *        An unmodifiable list of rooms
     */
    public List<ChatRoom> getRooms() {
        return connection.getRooms();
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    /**
     * The API Key to use when sending notifications.
     * This field is not required, but its needed if you plan to send notifications.
     * @return
     *        A <b>valid</b> API Key
     */
    public abstract String xmppHost();

}
