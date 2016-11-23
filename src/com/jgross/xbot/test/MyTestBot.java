package com.jgross.xbot.test;

import com.jgross.xbot.model.ChatRoom;
import com.jgross.xbot.model.XBot;

import java.util.Scanner;

public class MyTestBot extends XBot {

    @Override
    public void receiveMessage(String message, String from, ChatRoom chatRoom) {
        System.out.println(from + "(" + chatRoom.getTrueName() + ")" + ": " + message);
    }

    @Override
    public String username() {
        return "username";
    }

    @Override
    public String password() {
        return "password";
    }

    @Override
    public void onLoad() {
        boolean b = joinRoom("room name");
        if (b) {
            System.out.println("Joined " + getSelectedRoom().getXMPPName() + " !");
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
    public String apiKey() {
        return "apikey(optional)";
    }
}
