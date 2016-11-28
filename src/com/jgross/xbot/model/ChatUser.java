package com.jgross.xbot.model;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

public class ChatUser {
    private String user_jid;
    private String name;
    private String nickname;
    private String email;
    private String photo;
    private String status;
    private String status_message;

    /**
     * Return the ChatUser object that has the user JID of <b>userJID</b>
     * @param nickname
     *            The user's nickname
     * @param userJID
     *            The user's Jabber ID
     * @return
     *        The ChatUser object with the given user id.
     */
    public static ChatUser createInstance(String nickname, String userJID) {
        ChatUser user = new ChatUser();
        user.nickname = nickname;
        user.user_jid = userJID;
        user.name = nickname;
        return user;
    }

    /**
     * Return the ChatUser object that has the user JID of <b>userJID</b>
     * @param userJID
     *            The user's Jabber ID
     * @param vCard
     *            The user's vCard
     * @return
     *        The ChatUser object with the given user id.
     */
    public static ChatUser createInstance(String userJID, VCard vCard) {

        ChatUser user = new ChatUser();

        user.user_jid = userJID;
        if (vCard.getField("FN") != null) {
            user.name = vCard.getField("FN");
        } else if (vCard.getNickName() != null){
            user.name = vCard.getNickName();
        } else {
            user.name = userJID;
        }

        if (vCard.getEmailHome() != null)
            user.email = vCard.getEmailHome();
        else if (vCard.getField("USERID") != null)
            user.email = vCard.getField("USERID");

        if (vCard.getAvatarHash() != null)
            user.photo = vCard.getAvatarHash();

        if (vCard.getNickName() != null)
            user.nickname = vCard.getNickName();

        return user;
    }

    /**
     * Return the ChatUser object that has the user JID of <b>userJID</b>
     * @param userJID
     *            The user's Jabber ID
     * @param connection
     *            XMPPTCPConnection used for getting vCard
     * @return
     *        The ChatUser object with the given user id.
     */
    public static ChatUser createInstance(String nickname, String userJID, XMPPTCPConnection connection) {
        ChatUser user;

        VCardManager vCardManager = VCardManager.getInstanceFor(connection);
        VCard vCard = null;

        try {
            vCard = vCardManager.loadVCard(userJID);
        } catch (SmackException.NoResponseException | XMPPException.XMPPErrorException | SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

        if (null != vCard)
            user = ChatUser.createInstance(userJID, vCard);
        else
            user = ChatUser.createInstance(nickname, userJID);

        return user;
    }
    
    private ChatUser() { }
    
    public String getUserJID() {
        return user_jid;
    }
    
    public String getName() {
        return name;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public String getEmail() {
        return email;
    }

    public String getPhoto() {
        return photo;
    }

    //TODO: Implement capturing status for user class
    public String getStatus() {
        return status;
    }
    
    public String getStatusMessage()  {
        return status_message;
    }

}
