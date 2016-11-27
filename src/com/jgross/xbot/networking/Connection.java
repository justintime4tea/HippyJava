package com.jgross.xbot.networking;

import com.jgross.xbot.XBotLib;
import com.jgross.xbot.eventsystem.events.model.MessageReceivedEvent;
import com.jgross.xbot.model.ChatRoom;
import com.jgross.xbot.networking.exceptions.LoginException;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Body;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public final class Connection implements ConnectionListener, ChatMessageListener {

    private static XMPPTCPConnection XMPP;
    private static XMPPTCPConnectionConfiguration CONNECTION_CONFIG;
    private boolean connected;
    private String password;
    private HashMap<ChatRoom, MultiUserChat> rooms = new HashMap<ChatRoom, MultiUserChat>();
    private HashMap<String, Chat> cache = new HashMap<String, Chat>();

    public Connection(XMPPTCPConnectionConfiguration config) {
        XMPP = new XMPPTCPConnection(config);
        CONNECTION_CONFIG = config;
    }
    
    public void connect() throws XMPPException, IOException, SmackException {
        if (connected)
            return;
        XMPP.connect();
        XMPP.addConnectionListener(this);
        connected = true;

        ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(XMPP);
        reconnectionManager.enableAutomaticReconnection();
    }
    
    public void login(String username, String password) throws LoginException {
        if (!connected)
            return;
        if (username.contains("@"))
            System.err.println("[XBotLib] The username being used looks like a Jabber ID. Try username without @host.domain.com");
        try {
            XMPP.login(username, password);
        } catch (XMPPException | SmackException | IOException exception) {
            throw new LoginException("There was an error logging in! Are you using the correct username (JID) / password?", exception);
        }
        this.password = password;
    }
    
    public void sendPM(String message, String to) throws XMPPException, SmackException.NotConnectedException {
        ChatManager chatMan = ChatManager.getInstanceFor(XMPP);
        Chat chat;
        if (cache.containsKey(to))
            chat = cache.get(to);
        else {
            chat = chatMan.createChat(to, this);
            cache.put(to, chat);
        }
        chat.sendMessage(message);
    }

    public void joinRoom(String host, String room, String nick) throws XMPPException.XMPPErrorException, SmackException {
        if (!connected || nick.equals("") || password.equals("") || rooms.containsKey(room))
            return;
        MultiUserChatManager mucMan = MultiUserChatManager.getInstanceFor(XMPP);
        MultiUserChat muc = mucMan.getMultiUserChat(room);
        final ChatRoom chatRoom = ChatRoom.createRoom(room, XMPP, true);
        rooms.put(chatRoom, muc);
    }

    public void joinRoom(String room, String nickname) throws XMPPException, SmackException {
        joinRoom(CONNECTION_CONFIG.getServiceName(), room, nickname);
    }
    
    public List<ChatRoom> getRooms() {
        ArrayList<ChatRoom> roomlist = new ArrayList<>();
        roomlist.addAll(rooms.keySet());
        return Collections.unmodifiableList(roomlist);
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
        return Roster.getInstanceFor(XMPP);
    }
    
    @Override
    public void processMessage(Chat arg0, Message arg1) {
        MessageReceivedEvent event = new MessageReceivedEvent(null, arg1);
        XBotLib.events.callEvent(event);
    }

    @Override
    public void connected(XMPPConnection xmppConnection) {

    }

    @Override
    public void authenticated(XMPPConnection xmppConnection, boolean b) {

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
