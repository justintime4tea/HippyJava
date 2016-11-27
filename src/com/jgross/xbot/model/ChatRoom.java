package com.jgross.xbot.model;

import com.jgross.xbot.XBotLib;
import com.jgross.xbot.eventsystem.events.model.MessageReceivedEvent;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.jivesoftware.smackx.xdata.Form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatRoom {

    private XMPPTCPConnection connection;
    private MultiUserChat chat;
    private RoomInfo info;
    private String subject;
    private String name;
    private ChatRoomInfo hinfo;
    private ArrayList<String> users = new ArrayList<String>();
    private int lastcount;
    private boolean halt;
    
    public static ChatRoom createRoom(String roomName, XMPPTCPConnection connection, boolean persistent) throws XMPPException.XMPPErrorException, SmackException {
        roomName = roomName.contains("@") ? roomName.substring(0, roomName.lastIndexOf("@")) : roomName;
        ChatRoom room = new ChatRoom(roomName);
        MultiUserChatManager chatManager = MultiUserChatManager.getInstanceFor(connection);
        room.chat = chatManager.getMultiUserChat(roomName + "@conference." + connection.getHost());

        // Create (or join) the room
        try {
            room.chat.createOrJoin(roomName);
            room.chat.addMessageListener(message -> {
                MessageReceivedEvent event = new MessageReceivedEvent(room, message);
                XBotLib.events.callEvent(event);
            });
            room.chat.addParticipantListener(presence -> {
                String chatUserNick = presence.getFrom().substring(presence.getFrom().indexOf("/") + 1, presence.getFrom().length());

                if (presence.getType().equals(Presence.Type.available)) {
//                    ChatUser chatUser = ChatUser.createInstance(chatUserNick);
//                    UserJoinedRoomEvent event = new UserJoinedRoomEvent(room, chatUser, chatUserNick);
//                    XBotLib.events.callEvent(event);
                }
                if (presence.getType().equals(Presence.Type.unavailable)) {
//                    ChatUser chatUser = ChatUser.createInstance(chatUserNick);
//                    UserLeftRoomEvent event = new UserLeftRoomEvent(room, chatUser, chatUserNick);
//                    XBotLib.events.callEvent(event);
                }



            });
        } catch (IllegalStateException e) {
            // Bot may already have joined the room.
        }
        // Get the the room's configuration form
        Form form = room.chat.getConfigurationForm();
        // Create a new form to submit based on the original form
        Form submitForm = form.createAnswerForm();

        // Send the completed form (with default values) to the server to configure the room
        submitForm.setAnswer("muc#roomconfig_membersonly", true);
        submitForm.setAnswer("muc#roomconfig_allow_subscription", true);
        submitForm.setAnswer("muc#roomconfig_roomname", roomName);
        submitForm.setAnswer("muc#roomconfig_persistentroom", persistent);

        room.chat.sendConfigurationForm(submitForm);
        room.info = chatManager.getRoomInfo(roomName + "@conference." + connection.getHost());
        room.connection = connection;

        return room;
    }
    
    private ChatRoom(String name, MultiUserChat chat) {
        this.name = name;
        this.chat = chat;
    }
    
    private ChatRoom(String name) {
        this.name = name;
    }
    
    public void disconnect() {
        //TODO Disconnect
    }
    
    /**
     * Get the current amount of useres in this room.
     * If this room is not connected, then this method may return -1.
     * To test the connection of this room, then use the method {@link ChatRoom#isConnected}
     * @return
     */
    public int getUserCount() {
        if (!isConnected())
            return -1;
        return chat.getOccupantsCount();
    }
    
    /**
     * Check to see if this room is able to send messages. This method (as of 0.1) only checks the
     * connection by checking to see if the {@link MultiUserChat} object is not null.
     */
    public boolean isConnected() {
        return chat != null;
    }
    
    /**
     * Get the XMPP name of this room. This does NOT include the full XMPP_JID for this room.
     * If you would like, then use {@link ChatRoom#getXMPP_JID}
     * @return
     */
    public String getXMPPName() {
        return (name.indexOf("@") != -1 ? name.split("\\@")[0] : name);
    }
    
    /**
     * Get the full XMPP_JID for this room.
     * @return
     */
    public String getXMPP_JID() {
        return (name.indexOf("@") != -1 ? name : name + "@" + connection.getHost());
    }
    
    /**
     * Get the true name for this room. The API key is used to connect to the hipchat API to get
     * the room information for this room. However, this is used as a fall back, if the room info has already been obtained recently, then
     * this parameter wont be used. If you think that you wont need an API Key for this call, then use {@link ChatRoom#getTrueName()}
     * @param APIKey
     *              The API Key for your hipchat account to obtain information for this room.
     * @return
     */
    public String getTrueName(String APIKey) {
        if (hinfo == null) {
            hinfo = ChatRoomInfo.getInfo(APIKey, this);
            if (hinfo == null)
                return null;
        }
        return hinfo.getRoomName();
    }
    
    /**
     * Get the true name for this room.
     * @return
     */
    public String getTrueName() {
        if (hinfo == null)
            return null;
        return hinfo.getRoomName();
    }
    
    /**
     * Get basic information from hipchat about this room.
     * @return
     */
    public ChatRoomInfo getHipchatRoomInfo() {
        return hinfo;
    }
    
    /**
     * Get basic information from hipchat about this room. The API key is used to connect to the hipchat API to get
     * the room information for this room. However, this is used as a fall back, if the room info has already been obtained recently, then
     * this parameter wont be used. If you think that you wont need an API Key for this call, then use {@link ChatRoom#getHipchatRoomInfo()}
     * @param APIKey
     *               The API Key for your hipchat account to obtain information for this room.
     * @return
     */
    public ChatRoomInfo getHipchatRoomInfo(String APIKey) {
        if (hinfo == null) {
            hinfo = ChatRoomInfo.getInfo(APIKey, this);
            if (hinfo == null)
                return null;
        }
        return hinfo;
    }
    
    /**
     * Get the current subject for this room. If the subject is null or equals "", then the subject will be gotten from the active connection
     * to the room. If no active connection is present, then it will fall back to using {@link ChatRoom#getHipchatRoomInfo()} to get the topic.
     * If that is null, then a null or empty subject may be returned.
     * @return
     */
    public String getSubject() {
        if (subject == null || subject.equals("")) {
            if (info != null)
                subject = info.getSubject();
            else if (hinfo != null)
                subject = hinfo.getTopic();
        }
        return subject;
    }
    
    /**
     * Set the subject for this room. This method may only be used when an active connection to the room is present.
     * @param subject
     * @return
     *        Returns whether the change was successful or not.
     */
    public boolean setSubject(String subject) {
        if (chat == null)
            return false;
        try {
            chat.changeSubject(subject);
        } catch (XMPPException e) {
            e.printStackTrace();
            return false;
        } catch (SmackException.NotConnectedException e) {
            // TODO: Add not connected message
            e.printStackTrace();
            return false;
        } catch (SmackException.NoResponseException e) {
            // TODO: Add No Response message
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public List<String> getConnectedUsers() {
        return Collections.unmodifiableList(chat.getOccupants());
    }
    
    /**
     * Send a message to this room. This method may only be used when an active connection to the room is present.
     * @param message
     *              The message to send.
     * @param from
     *            The name of the user who sent this message.
     * @return
     *        Whether the action was successful or not.
     */
    public boolean sendMessage(String message, String from) {
        if (chat == null)
            return false;
        try {
            chat.sendMessage(message);
            return true;
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void inviteUsers(List<String> users, String reason) {
        users.forEach(user -> {
            try {
                chat.invite(user, reason);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
        });
    }

}
