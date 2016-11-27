package com.jgross.xbot.model;

import java.util.HashMap;

public class ChatUser {
    private int user_id;
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
     * Return the ChatUser object that has the name of <b>nick</b>
     * @param nick
     *            The nick to get the users for
     * @param APIKey
     *             The API Key to use when using Hipchat's HTTP API
     * @return
     *        The ChatUser object with the given nick.
     */
    public static ChatUser createInstance(String nick, String APIKey) {
        // TODO: Replace HipChat with pure XMPP
        if (!user_cache.containsKey(nick)) {
            ChatUser[] users = getHipchatUsers(APIKey);
            for (ChatUser user : users) {
                if (!user_cache.containsKey(user.name))
                    user_cache.put(user.name, user);
            }
            if (!user_cache.containsKey(nick))
                return null;
            else
                return user_cache.get(nick);
        }
        else
            user_cache.get(nick);
        return null;
    }
    
    /**
     * Return the ChatUser object that has the user id of <b>ID</b>
     * @param ID
     *            The user id to get the users for
     * @param APIKey
     *             The API Key to use when using Hipchat's HTTP API
     * @return
     *        The ChatUser object with the given user id.
     */
    public static ChatUser createInstance(int ID, String APIKey) {
        //TODO: Replace HipChat with pure XMPP
        ChatUser[] users = getHipchatUsers(APIKey);
        for (ChatUser user : users) {
            if (user.user_id == ID)
                return user;
        }
        return null;
    }
    
    /**
     * Get an array of ChatUser objects that the APIKey passed in the parameter has
     * access to.
     * @param APIKey
     *              The API Key to use
     * @return
     *        An array of HipchatUsers
     */
    public static ChatUser[] getHipchatUsers(String APIKey) {
        return getHipchatUserHolder(APIKey).users;
    }
    
    private static HipchatUserHolder getHipchatUserHolder(String APIKey) {
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
    
    public int getUserID() {
        return user_id;
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

    public static ChatUser[] getChatUsers(String s) {
        return new ChatUser[0];
    }

    private static class HipchatUserHolder {
        public ChatUser[] users;
        public HipchatUserHolder() { }
    }
}
