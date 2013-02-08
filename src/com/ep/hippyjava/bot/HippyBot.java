package com.ep.hippyjava.bot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;

import com.ep.hippyjava.HippyJava;
import com.ep.hippyjava.eventsystem.EventHandler;
import com.ep.hippyjava.eventsystem.Listener;
import com.ep.hippyjava.eventsystem.events.MessageRecivedEvent;
import com.ep.hippyjava.networking.Connection;
import com.ep.hippyjava.networking.Room;
import com.ep.hippyjava.utils.NotificationColor;
import com.ep.hippyjava.utils.NotificationType;

public abstract class HippyBot implements Bot, Listener {

    protected Connection con;
    private Room selected;

    @Override
    public void run(Connection con) {
        HippyJava.events.registerEvents(new Listener() {
            @SuppressWarnings("unused")
            @EventHandler
            public void messageEvent(MessageRecivedEvent event) {
                recieveMessage(event.body(), event.from(), event.getRoom());
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

    public void changeRoom(String name) {
        if (findRoom(name) != null)
            changeRoom(findRoom(name));
    }

    public boolean joinRoom(String name) {
        try {
            con.joinRoom(name, nickname());
            selected = findRoom(name);
            return true;
        } catch (XMPPException e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public Room getSelectedRoom() {
        return selected;
    }

    @Override
    public void changeRoom(Room room) {
        this.selected = room;
    }

    public void sendMessageToRoom(String name, String message) {
        if (findRoom(name) != null)
            sendMessage(message, findRoom(name));
    }

    @Override
    public void sendMessage(String message, Room room) {
        room.sendMessage(message, nickname());
    }

    @Override
    public List<String> users() {
        final Roster r = con.getRoster();
        ArrayList<String> users = new ArrayList<String>();
        for (RosterEntry e : r.getEntries())
            users.add(e.getName());
                return Collections.unmodifiableList(users);
    }

    /**
     * Send a hipchat notification to the currently selected room specified in {@link Bot#getSelectedRoom()} with the name specified in {@link Bot#nickname()}. If no room is selected, then the message is not sent.
     * This method will only accept normal text as input,
     * if you wish to use HTML, please use {@link HippyBot#sendNotification(String, String, Room, NotificationType). </br>
     * The background color for this notification will be set to {@link NotificationColor#YELLOW} and users will be notified when this
     * notification is sent.
     * @param message
     *               The body of the message to send. You may input html into this by setting the
     *               type parameter to {@link NotificationType#HTML}
     * @throws IOException 
     */
    public String sendNotification(String message) {
        if (selected == null)   
            return "{\"status\": \"failed\"}";
        return sendNotification(message, nickname(), selected, NotificationType.TEXT, true, NotificationColor.YELLOW);
    }

    /**
     * Send a hipchat notification to the currently selected room specified in {@link Bot#getSelectedRoom()}. If no room is selected, then the message is not sent.
     * This method will only accept normal text as input,
     * if you wish to use HTML, please use {@link HippyBot#sendNotification(String, String, Room, NotificationType). </br>
     * The background color for this notification will be set to {@link NotificationColor#YELLOW} and users will be notified when this
     * notification is sent.
     * @param message
     *               The body of the message to send. You may input html into this by setting the
     *               type parameter to {@link NotificationType#HTML}
     * @param from
     *            The name to use in the notification.
     * @throws IOException 
     */
    public String sendNotification(String message, String from) {
        if (selected == null)
            return "{\"status\": \"failed\"}";
        return sendNotification(message, from, selected, NotificationType.TEXT, true, NotificationColor.YELLOW);
    }

    /**
     * Send a hipchat notification to the room specified. This method will only accept normal text as input,
     * if you wish to use HTML, please use {@link HippyBot#sendNotification(String, String, Room, NotificationType). </br>
     * The background color for this notification will be set to {@link NotificationColor#YELLOW} and users will be notified when this
     * notification is sent.
     * @param message
     *               The body of the message to send. You may input html into this by setting the
     *               type parameter to {@link NotificationType#HTML}
     * @param from
     *            The name to use in the notification.
     * @param room
     *            The room to send this notification to.
     * @throws IOException 
     */
    public String sendNotification(String message, String from, Room room) {
        return sendNotification(message, from, room, NotificationType.HTML, true, NotificationColor.YELLOW);
    }

    /**
     * Send a hipchat notification to the room specified. This method will only accept normal text as input,
     * if you wish to use HTML, please use {@link HippyBot#sendNotification(String, String, Room, NotificationType)}
     * The background color for this notification will be set to {@link NotificationColor#YELLOW} and users will be notified when this
     * notification is sent.
     * @param message
     *               The body of the message to send. You may input html into this by setting the
     *               type parameter to {@link NotificationType#HTML}
     * @param from
     *            The name to use in the notification.
     * @param room
     *            The name of the room to send this notification to.
     * @throws IOException 
     */
    public String sendNotification(String message, String from, String room) {
        Room r = findRoom(room);
        if (r == null)
            return "{\"status\": \"failed\"}";
        return sendNotification(message, from, r, NotificationType.HTML, true, NotificationColor.YELLOW);
    }

    /**
     * Send a hipchat notification to the room specified. You may use HTML if the {@link NotificationType} in the param is
     * set to {@link NotificationType#HTML}. </br>
     * The background color for this notification will be set to {@link NotificationColor#YELLOW} and users will be notified when this
     * notification is sent.
     * @param message
     *               The body of the message to send. You may input html into this by setting the
     *               type parameter to {@link NotificationType#HTML}
     * @param from
     *            The name to use in the notification.
     * @param room
     *            The room to send this notification to.
     * @param type
     *            The type of message to send. If {@link NotificationType#HTML} is chosen, then this message receives no special treatment.
     *            This must be valid HTML and entities must be escaped. @see  NotificationType#HTML for more info. </br>
     *            If {@link NotificationType#TEXT} is chosen, then this message will be treated just like a normal message from a user.
     * @throws IOException 
     *            @see NotificationType#TEXT for more info.
     */
    public String sendNotification(String message, String from, Room room, NotificationType type) {
        return sendNotification(message, from, room, type, true, NotificationColor.YELLOW);
    }

    /**
     * Send a hipchat notification to the room specified. You may use HTML if the {@link NotificationType} in the param is
     * set to {@link NotificationType#HTML}. </br>
     * The background color for this notification will be set to {@link NotificationColor#YELLOW}
     * @param message
     *               The body of the message to send. You may input html into this by setting the
     *               type parameter to {@link NotificationType#HTML}
     * @param from
     *            The name to use in the notification.
     * @param room
     *            The room to send this notification to.
     * @param type
     *            The type of message to send. If {@link NotificationType#HTML} is chosen, then this message receives no special treatment.
     *            This must be valid HTML and entities must be escaped. @see  NotificationType#HTML for more info. </br>
     *            If {@link NotificationType#TEXT} is chosen, then this message will be treated just like a normal message from a user.
     *            @see NotificationType#TEXT for more info.
     * @param notifyusers
     *                   Whether users should be notified when this notification is sent (Change tab color, play a sound, ect).
     * @throws IOException 
     */
    public String sendNotification(String message, String from, Room room, NotificationType type, boolean notifyusers) {
        return sendNotification(message, from, room, type, notifyusers, NotificationColor.YELLOW);
    }

    /**
     * Send a hipchat notification to the room specified. You may use HTML if the {@link NotificationType} in the param is
     * set to {@link NotificationType#HTML}. </br>
     * @param message
     *               The body of the message to send. You may input html into this by setting the
     *               type parameter to {@link NotificationType#HTML}
     * @param from
     *            The name to use in the notification.
     * @param room
     *            The room to send this notification to.
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
    public String sendNotification(String message, String from, Room room, NotificationType type, boolean notifyusers, NotificationColor color) {
        try {
            URL url = new URL("https://api.hipchat.com/v1/rooms/message?format=json&auth_token=" + apiKey());
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            String tosend = "room_id=" + room.getHipchatRoomInfo(apiKey()).getID() + "&from=" + from + "&message=" + message.replaceAll(" ", "+") + "&message_format=" + type.getType() + "&notify=" + notifyusers + "&color=" + color.getType();
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

    public Room findRoom(String name) {
        Room r = con.findConnectedRoom(name);
        if (r == null) {
            for (Room room : con.getRooms()) {
                if (room.getTrueName(apiKey()).equals(name))
                    return room;
            }
            return null;
        }
        else
            return r;
    }

    public List<Room> getRooms() {
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
     */
    public abstract String apiKey();
}