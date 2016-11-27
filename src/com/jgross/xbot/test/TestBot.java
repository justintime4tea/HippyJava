package com.jgross.xbot.test;

import com.jgross.xbot.model.ChatRoom;
import com.jgross.xbot.model.XBot;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TestBot extends XBot {

    @Override
    public void receiveMessage(String message, String from, ChatRoom chatRoom) {
        System.out.println(from + "(" + chatRoom.getTrueName() + ")" + ": " + message);
    }

    @Override
    public String username() {
        return "username"; // Without full JID, no @host.domain.com
    }

    @Override
    public String password() {
        return "password";
    }

    @Override
    public void onLoad() {

        List<String> users = new ArrayList<>();

        users.add("user" + "@" + xmppHost());

        boolean b = false;
        try {
            b = joinRoom("TestBotLibConversion");
        } catch (XMPPException.XMPPErrorException | SmackException e) {
            e.printStackTrace();
        }
        if (b) {
            System.out.println("Joined " + getSelectedRoom().getXMPPName() + " !");
            getSelectedRoom().inviteUsers(users, "Join the fun!");
            users.forEach(user -> {
                System.out.println("Invited " + user + " to the chat!");
            });
        }
        else
            System.out.println("I didnt join :(");
        new Thread() {
            
            @Override
            public void run() {
                final Scanner scan = new Scanner(System.in);
                while (true) {
                    String line = scan.nextLine();
                    sendMessage(line);
                }
            }
        }.start();
    }

    @Override
    public String nickname() {
        return "display name";
    }

    @Override
    public String xmppHost() {
        return "host.domain.com";
    }

    @Override
    public XMPPTCPConnectionConfiguration connectionConfig() {
        return XMPPTCPConnectionConfiguration.builder()
                .setServiceName(xmppHost())
                .setSendPresence(false)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.required)
                .setHost(xmppHost())
                .build();
    }
}
