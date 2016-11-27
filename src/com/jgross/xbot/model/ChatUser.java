package com.jgross.xbot.model;

import java.util.HashMap;

public class ChatUser {
    private String user_jid;
    private String name;
    private String mention_name;
    private String email;
    private String title;
    private String photo_url;
    private String status;
    private String status_message;
    private int is_group_admin;
    private int is_deleted;
    private static HashMap<String, ChatUser> user_cache = new HashMap<String, ChatUser>();

    /**
     * Return the ChatUser object that has the user JID of <b>userJID</b>
     * @param userJID
     *            The user's Jabber ID
     * @return
     *        The ChatUser object with the given user id.
     */
    public static ChatUser createInstance(String userJID) {
        //TODO: Replace HipChat with pure XMPP
        ChatUser[] users = getChatUsers();
        for (ChatUser user : users) {
            if (user.user_jid.equals(userJID))
                return user;
        }
        return null;
    }

    private static ChatUserHolder getChatUserHolder(String APIKey) {
        //TODO: Replace or Remove (HipChat)
//        try {
//            String JSON = WebUtils.getTextAsString("https://api.hipchat.com/v1/users/list?format=json&auth_token=" + APIKey);
//            HipchatUserHolder data = GSON.fromJson(JSON, HipchatUserHolder.class);
//            return data;
//        } catch (Exception e) {
//            e.printStackTrace();
//            HipchatUserHolder u = new HipchatUserHolder();
//            u.users = new ChatUser[0];
//            return u;
//        }
        return null;
    }
    
    private ChatUser() { }
    
    public String getUserJID() {
        return user_jid;
    }
    
    public String getName() {
        return name;
    }
    
    public String getMentionName() {
        return mention_name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getPhotoUrl() {
        return photo_url;
    }
    
    public String getStatus() {
        return status;
    }
    
    public String getStatusMessage()  {
        return status_message;
    }
    
    public boolean isGroupAdmin() {
        return is_group_admin == 1;
    }
    
    public boolean isDeletedAccount() {
        return is_deleted == 1;
    }

    public static ChatUser[] getChatUsers() {
        return new ChatUser[0];
    }

    public static ChatUser[] getChatUsers(String host) {
        return new ChatUser[0];
    }

    private static class ChatUserHolder {
        public ChatUser[] users;
        public ChatUserHolder() { }
    }
}
