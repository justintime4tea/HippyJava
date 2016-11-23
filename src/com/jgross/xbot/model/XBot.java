package com.jgross.xbot.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jgross.xbot.XBotLib;
import com.jgross.xbot.eventsystem.Listener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;

import com.jgross.xbot.eventsystem.EventHandler;
import com.jgross.xbot.eventsystem.events.model.MessageRecivedEvent;
import com.jgross.xbot.networking.Connection;
import com.jgross.xbot.utils.NotificationColor;
import com.jgross.xbot.utils.NotificationType;

public abstract class XBot implements Bot, Listener {

    private Connection con;
    private ChatRoom selected;

    @Override
    public void run(Connection con) {
        XBotLib.events.registerEvents(new Listener() {
            @SuppressWarnings("unused")
            @EventHandler
            public void messageEvent(MessageRecivedEvent event) {
                receiveMessage(event.body(), event.from(), event.getChatRoom());
            }
        });
        this.con = con;
        try {
            con.connect();
            con.login(username(), password());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        onLoad();
    }

    @Override
    public void sendMessage(String message) {
        if (selected == null)
            return;
        sendMessage(message, selected);
    }

    /**
     * Change the currently selected room. You must be in the room in order to change to it, to join a room, invoke the method
     * {@link XBot#joinRoom(String)}.
     * The room name provide must be the JID name of the room. If {@link XBot#apiKey()} returns a valid
     * API Key, then the room name provided can be the normal name of the room.
     * @param name
     */
    public void changeRoom(String name) {
        if (findRoom(name) != null)
            changeRoom(findRoom(name));
    }
    
    /**
     * Join a room with the given name.
     * The room name provided must be the JID name of the room. If {@link XBot#apiKey()} returns a valid API Key, then
     * the room name provided can be the normal name of the room.
     * @param name
     * @return
     *        Returns whether the operation was successful or not.
     */
    public boolean joinRoom(String name) {
        try {
            if (apiKey().equals(""))
                con.joinRoom(name, nickname());
            else
                con.joinRoom(apiKey(), name, nickname());
            selected = findRoom(name);
            return true;
        } catch (XMPPException e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public ChatRoom getSelectedRoom() {
        return selected;
    }

    @Override
    public void changeRoom(ChatRoom chatRoom) {
        this.selected = chatRoom;
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
        if (to.indexOf("@") == -1) { //oh noes its not a JID! The user didnt follow the rules!
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
     * @param to
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
     * In order for this method to work properly, the {@link XBot#apiKey()} method must return a valid API Key, otherwise this method will
     * return an empty list.
     * @return
     */
    @Override
    public List<ChatUser> getUsers() {
        ArrayList<ChatUser> users = new ArrayList<ChatUser>();
        if (apiKey().equals(""))
            return Collections.unmodifiableList(users);
        ChatUser[] u = ChatUser.getHipchatUsers(apiKey());
        for (ChatUser user : u) {
            users.add(user);
        }
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
     * Send a hipchat notification to the currently selected room specified in {@link XBot#getSelectedRoom()} with the name specified in {@link XBot#nickname()}. If no room is selected, then the message is not sent.
     * This method will only accept normal text as input,
     * if you wish to use HTML, please use {@link XBot#sendNotification(String, String, ChatRoom, NotificationType). </br>
     * The background color for this notification will be set to {@link NotificationColor#YELLOW} and users will be notified when this
     * notification is sent. </br>
     * <b>In order to use this method, {@link XBot#apiKey()} must return a valid API Key!</b>
     * @param message
     *               The body of the message to send.
     * @throws IOException 
     * @return The json response from the server
     */
    public String sendNotification(String message) {
        if (selected == null)   
            return "{\"status\": \"failed\"}";
        return sendNotification(message, nickname(), selected, NotificationType.TEXT, true, NotificationColor.YELLOW);
    }

    /**
     * Send a hipchat notification to the currently selected room specified in {@link XBot#getSelectedRoom()}. If no room is selected, then the message is not sent.
     * This method will only accept normal text as input,
     * if you wish to use HTML, please use {@link XBot#sendNotification(String, String, ChatRoom, NotificationType). </br>
     * The background color for this notification will be set to {@link NotificationColor#YELLOW} and users will be notified when this
     * notification is sent. </br>
     * <b>In order to use this method, {@link XBot#apiKey()} must return a valid API Key!</b>
     * @param message
     *               The body of the message to send.
     * @param from
     *            The name to use in the notification.
     * @throws IOException
     * @return The json response from the server 
     */
    public String sendNotification(String message, String from) {
        if (selected == null)
            return "{\"status\": \"failed\"}";
        return sendNotification(message, from, selected, NotificationType.TEXT, true, NotificationColor.YELLOW);
    }

    /**
     * Send a hipchat notification to the chatRoom specified. This method will only accept normal text as input,
     * if you wish to use HTML, please use {@link XBot#sendNotification(String, String, ChatRoom, NotificationType). </br>
     * The background color for this notification will be set to {@link NotificationColor#YELLOW} and users will be notified when this
     * notification is sent. </br>
     * <b>In order to use this method, {@link XBot#apiKey()} must return a valid API Key!</b>
     * @param message
     *               The body of the message to send.
     * @param from
     *            The name to use in the notification.
     * @param room
     *            The room to send this notification to.
     * @throws IOException 
     * @return The json response from the server
     */
    public String sendNotification(String message, String from, ChatRoom chatRoom) {
        return sendNotification(message, from, chatRoom, NotificationType.TEXT, true, NotificationColor.YELLOW);
    }

    /**
     * Send a hipchat notification to the room specified. This method will only accept normal text as input,
     * if you wish to use HTML, please use {@link XBot#sendNotification(String, String, ChatRoom, NotificationType)}
     * The background color for this notification will be set to {@link NotificationColor#YELLOW} and users will be notified when this
     * notification is sent. </br>
     * <b>In order to use this method, {@link XBot#apiKey()} must return a valid API Key!</b>
     * @param message
     *               The body of the message to send.
     * @param from
     *            The name to use in the notification.
     * @param room
     *            The name of the room to send this notification to.
     * @throws IOException 
     * @return The json response from the server
     */
    public String sendNotification(String message, String from, String room) {
        ChatRoom r = findRoom(room);
        if (r == null)
            return "{\"status\": \"failed\"}";
        return sendNotification(message, from, r, NotificationType.TEXT, true, NotificationColor.YELLOW);
    }

    /**
     * Send a hipchat notification to the chatRoom specified. You may use HTML if the {@link NotificationType} in the param is
     * set to {@link NotificationType#HTML}. </br>
     * The background color for this notification will be set to {@link NotificationColor#YELLOW} and users will be notified when this
     * notification is sent. </br>
     * <b>In order to use this method, {@link XBot#apiKey()} must return a valid API Key!</b>
     * @param message
     *               The body of the message to send. You may input html into this by setting the
     *               type parameter to {@link NotificationType#HTML}
     * @param from
     *            The name to use in the notification.
     * @param chatRoom
     *            The chatRoom to send this notification to.
     * @param type
     *            The type of message to send. If {@link NotificationType#HTML} is chosen, then this message receives no special treatment.
     *            This must be valid HTML and entities must be escaped. @see  NotificationType#HTML for more info. </br>
     *            If {@link NotificationType#TEXT} is chosen, then this message will be treated just like a normal message from a user.
     * @throws IOException 
     * @return The json response from the server
     */
    public String sendNotification(String message, String from, ChatRoom chatRoom, NotificationType type) {
        return sendNotification(message, from, chatRoom, type, true, NotificationColor.YELLOW);
    }

    /**
     * Send a hipchat notification to the chatRoom specified. You may use HTML if the {@link NotificationType} in the param is
     * set to {@link NotificationType#HTML}. </br>
     * The background color for this notification will be set to {@link NotificationColor#YELLOW} </br>
     * <b>In order to use this method, {@link XBot#apiKey()} must return a valid API Key!</b>
     * @param message
     *               The body of the message to send. You may input html into this by setting the
     *               type parameter to {@link NotificationType#HTML}
     * @param from
     *            The name to use in the notification.
     * @param chatRoom
     *            The chatRoom to send this notification to.
     * @param type
     *            The type of message to send. If {@link NotificationType#HTML} is chosen, then this message receives no special treatment.
     *            This must be valid HTML and entities must be escaped. @see  NotificationType#HTML for more info. </br>
     *            If {@link NotificationType#TEXT} is chosen, then this message will be treated just like a normal message from a user.
     *            @see NotificationType#TEXT for more info.
     * @param notifyusers
     *                   Whether users should be notified when this notification is sent (Change tab color, play a sound, ect).
     * @throws IOException 
     * @return The json response from the server
     */
    public String sendNotification(String message, String from, ChatRoom chatRoom, NotificationType type, boolean notifyusers) {
        return sendNotification(message, from, chatRoom, type, notifyusers, NotificationColor.YELLOW);
    }

    /**
     * Send a hipchat notification to the chatRoom specified. You may use HTML if the {@link NotificationType} in the param is
     * set to {@link NotificationType#HTML}. </br>
     * <b>In order to use this method, {@link XBot#apiKey()} must return a valid API Key!</b>
     * @param message
     *               The body of the message to send. You may input html into this by setting the
     *               type parameter to {@link NotificationType#HTML}
     * @param from
     *            The name to use in the notification.
     * @param chatRoom
     *            The chatRoom to send this notification to.
     * @param type
     *            The type of message to send. If {@link NotificationType#HTML} is chosen, then this message receives no special treatment.
     *            This must be valid HTML and entities must be escaped. @see  NotificationType#HTML for more info. </br>
     *            If {@link NotificationType#TEXT} is chosen, then this message will be treated just like a normal message from a user.
     *            @see NotificationType#TEXT for more info.
     * @param notifyusers
     *                   Whether users should be notified when this notification is sent (Change tab color, play a sound, ect).
     * @param color
     *             The background color for the message.
     * @return The json response from the server
     * @throws IOException 
     */
    public String sendNotification(String message, String from, ChatRoom chatRoom, NotificationType type, boolean notifyusers, NotificationColor color) {
        try {
            URL url = new URL("https://api.hipchat.com/v1/rooms/message?format=json&auth_token=" + apiKey());
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            String tosend = "room_id=" + chatRoom.getHipchatRoomInfo(apiKey()).getID() + "&from=" + from + "&message=" + message.replaceAll(" ", "+") + "&message_format=" + type.getType() + "&notify=" + notifyusers + "&color=" + color.getType();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Length", "" + tosend.length());
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(tosend);
            writer.close();
            BufferedReader read = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder builder = new StringBuilder(100);
            String line;
            while ((line = read.readLine()) != null)
                builder.append(line);
            read.close();
            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"status\": \"failed\"}";
        }
    }

    /**
     * Look for a room that you are currently connected to
     * @param name
     *            The name of the room to look for
     * @return
     *        The room
     */
    public ChatRoom findRoom(String name) {
        ChatRoom r = con.findConnectedRoom(name);
        if (r == null) {
            for (ChatRoom chatRoom : con.getRooms()) {
                if (chatRoom.getTrueName(apiKey()).equals(name))
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
        return con.getRooms();
    }

    @Override
    public Connection getConnection() {
        return con;
    }

    /**
     * The API Key to use when sending notifications.
     * This field is not required, but its needed if you plan to send notifications.
     * @return
     *        A <b>valid</b> API Key
     */
    public abstract String apiKey();
}
