package com.jgross.xbot.networking;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.jgross.xbot.model.ChatRoom;
import com.jgross.xbot.networking.exceptions.LoginException;
import com.jgross.xbot.XBotLib;
import com.jgross.xbot.utils.Constants;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Body;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.MultiUserChat;

import com.jgross.xbot.eventsystem.events.model.MessageRecivedEvent;

public final class Connection implements MessageListener, ConnectionListener {
    
    private static final ConnectionConfiguration CONNECTION_CONFIG = new ConnectionConfiguration(Constants.XMPP_URL, Constants.PORT);
    private final XMPPConnection XMPP = new XMPPConnection(CONNECTION_CONFIG);
    private boolean connected;
    private String password;
    private HashMap<ChatRoom, MultiUserChat> rooms = new HashMap<ChatRoom, MultiUserChat>();
    private HashMap<String, Chat> cache = new HashMap<String, Chat>();
    
    public void connect() throws XMPPException {
        if (connected)
            return;
        XMPP.connect();
        XMPP.addConnectionListener(this);
        connected = true;
    }
    
    public void login(String username, String password) throws LoginException {
        if (!connected)
            return;
        if (!username.contains("hipchat.com"))
            System.err.println("[XBotLib] The username being used does not look like a Jabber ID. Are you sure this is the correct username?");
        try {
            XMPP.login(username, password);
        } catch (XMPPException exception) {
            throw new LoginException("There was an error logging in! Are you using the correct username/password?", exception);
        }
        this.password = password;
    }
    
    public void sendPM(String message, String to) throws XMPPException {
        Chat c;
        if (cache.containsKey(to))
            c = cache.get(to);
        else {
            c = XMPP.getChatManager().createChat(to, this);
            cache.put(to, c);
        }
        c.sendMessage(message);
    }
    
    public void joinRoom(String room, String nickname) throws XMPPException {
        joinRoom("", room, nickname);
    }
    
    public void joinRoom(String APIKey, String room, String nickname) throws XMPPException {
        if (!connected || nickname.equals("") || password.equals("") || rooms.containsKey(room))
            return;
        MultiUserChat muc2;
        if (!isJID(room)) {
            ChatRoom temp = ChatRoom.createRoom(APIKey, room);
            room = temp.getHipchatRoomInfo(APIKey).getJID();
            muc2 = new MultiUserChat(XMPP, room);
            temp = null;
        }
        else
            muc2 = new MultiUserChat(XMPP, (room.indexOf("@") != -1 ? room : room + "@" + Constants.CONF_URL));
        muc2.join(nickname, password);
        final ChatRoom obj = ChatRoom.createRoom(APIKey, room, muc2, XMPP);
        muc2.addMessageListener(new PacketListener() {

            @Override
            public void processPacket(Packet paramPacket) {
                Message m = new Message();
                m.setBody(toMessage(paramPacket));
                m.setFrom(paramPacket.getFrom().split("\\/")[1]);
                MessageRecivedEvent event = new MessageRecivedEvent(obj, m);
                XBotLib.events.callEvent(event);
            } 
        });
        rooms.put(obj, muc2);
    }
    
    public List<ChatRoom> getRooms() {
        ArrayList<ChatRoom> roomlist = new ArrayList<ChatRoom>();
        for (ChatRoom chatRoom : rooms.keySet()) {
            roomlist.add(chatRoom);
        }
        return Collections.unmodifiableList(roomlist);
    }
    
    public boolean sendMessageToRoom(String room, String message, String nickname) {
        if (!rooms.containsKey(room)) {
            try {
                joinRoom(room, nickname);
            } catch (XMPPException e) {
                e.printStackTrace();
                return false;
            }
        }
        ChatRoom obj;
        if ((obj = findConnectedRoom(room)) != null)
            return obj.sendMessage(message, nickname);
        return false;
    }
    
    public ChatRoom findConnectedRoom(String name) {
        for (ChatRoom r : getRooms()) {
            if (r.getXMPPName().equals(name))
                return r;
        }
        return null;
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public void disconnect() {
        if (!connected)
            return;
        XMPP.disconnect();
        connected = false;
    }
    
    public Roster getRoster() {
        return XMPP.getRoster();
    }
    
    @Override
    public void processMessage(Chat arg0, Message arg1) {
        MessageRecivedEvent event = new MessageRecivedEvent(null, arg1);
        XBotLib.events.callEvent(event);
    }

    @Override
    public void connectionClosed() {
        connected = false;
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        connected = false;
    }

    @Override
    public void reconnectingIn(int seconds) {
        
    }

    @Override
    public void reconnectionFailed(Exception e) {
        if (connected)
            connected = false;
    }

    @Override
    public void reconnectionSuccessful() {
        if (!connected)
            connected = true;
    }
    
    public synchronized void waitForEnd() throws InterruptedException {
        while (true) {
            if (!connected)
                break;
            super.wait(0L);
        }
    }
    
    private boolean isJID(String name) {
        try {
            Integer.parseInt(name.split("\\_")[0]);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private String toMessage(Packet packet) {
        try {
            Field f = packet.getClass().getDeclaredField("bodies");
            f.setAccessible(true);
            @SuppressWarnings("rawtypes")
            HashSet h = (HashSet)f.get(packet);
            if (h.size() == 0)
                return "";
            for (Object obj : h) {
                if (obj instanceof Body)
                    return ((Body)obj).getMessage();
            }
            return "";
        } catch (Exception e) {
            return "";
        }
        
    }
}
