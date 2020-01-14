package com.example.wetalk.Classes;

public class Contacts {
    private String name;
    private String phone;
    private String image;
    private String lastMessage;
    private String lastMessageTime;
    private int unreadMessages;

    public Contacts() {
        this.setPhone(null);
        this.setLastMessage(null);
        this.setName(null);
        this.setImage(null);
        this.setUnreadMessages(0);
        this.setLastMessageTime(null);
    }

    public Contacts(String name, String phone, String image, String lastMessage, int unreadMessages, String lastMessageTime){
        this.setPhone(phone);
        this.setLastMessage(lastMessage);
        this.setName(name);
        this.setImage(image);
        this.setUnreadMessages(unreadMessages);
        this.setLastMessageTime(lastMessageTime);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public int getUnreadMessages() {
        return unreadMessages;
    }

    public void setUnreadMessages(int unreadMessages) {
        this.unreadMessages = unreadMessages;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
}
