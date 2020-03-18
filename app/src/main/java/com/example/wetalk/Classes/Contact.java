package com.example.wetalk.Classes;

public class Contact {
    private String userId;
    private String rawId;
    private String name;
    private String phone;
    private String image;
    private String status;
    private String lastMessage;
    private String lastMessageTime;
    private int unreadMessages;

    public Contact(Contact contact) {
        this.setPhone(contact.getPhone());
        this.setName(contact.getName());
        this.setLastMessage(contact.getLastMessage());
        this.setImage(contact.getImage());
        this.setUnreadMessages(contact.getUnreadMessages());
        this.setLastMessageTime(contact.getLastMessageTime());
        this.setStatus(contact.getStatus());
    }

    public Contact() {
        this.setPhone(null);
        this.setLastMessage(null);
        this.setName(null);
        this.setImage(null);
        this.setUnreadMessages(0);
        this.setLastMessageTime(null);
        this.setStatus(null);
    }

    public Contact(String userId, String rawId, String name, String phone, String status, String image) {
       this.setRawId(rawId);
       this.setName(name);
       this.setPhone(phone);
       this.setStatus(status);
       this.setImage(image);
       this.setUserId(userId);
    }

    public Contact(String name, String phone, String status) {
        this.setPhone(phone);
        this.setName(name);
        this.setLastMessage(null);
        this.setImage(null);
        this.setUnreadMessages(0);
        this.setLastMessageTime(null);
        this.setStatus(status);
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRawId() {
        return rawId;
    }

    public void setRawId(String rawId) {
        this.rawId = rawId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
