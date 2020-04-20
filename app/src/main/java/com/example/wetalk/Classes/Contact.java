package com.example.wetalk.Classes;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class Contact implements Serializable {
    private String userId;
    private String rawId;
    private String name;
    private String phone;
    private String image;
    private String status;
    private Message lastMessage;
    private int unreadMessages;

    public Contact(Contact contact) {
        this.setPhone(contact.getPhone());
        this.setName(contact.getName());
        this.setImage(contact.getImage());
        this.setUnreadMessages(contact.getUnreadMessages());
        this.setStatus(contact.getStatus());
        this.setRawId((contact.getRawId()));
        this.setUserId(contact.getUserId());
        this.setLastMessage(contact.getLastMessage());
    }

    public Contact() {
        this.setPhone(null);
        this.setName(null);
        this.setImage(null);
        this.setUnreadMessages(0);
        this.setStatus(null);
    }

    public Contact(String userId, String rawId, String name, String phone, String status, String image, Message lastMessage, int unreadMessages) {
       this.setRawId(rawId);
       this.setName(name);
       this.setPhone(phone);
       this.setStatus(status);
       this.setImage(image);
       this.setUserId(userId);
       this.setUnreadMessages(unreadMessages);
       this.setLastMessage(lastMessage);
    }

    public Contact(String name, String phone, String status) {
        this.setPhone(phone);
        this.setName(name);
        this.setImage(null);
        this.setUnreadMessages(0);
        this.setStatus(status);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        Contact contact = (Contact) obj;

        return (this.name.equals(contact.name)) && (this.userId.equals(contact.userId))
                && (this.rawId.equals(contact.rawId)) && (this.image == contact.image)
                && (this.status.equals(contact.status) && (this.unreadMessages == contact.unreadMessages)
                && (this.phone.equals(contact.phone)) && (this.lastMessage.getMessageId().equals(contact.lastMessage.getMessageId())));
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

    public int getUnreadMessages() {
        return unreadMessages;
    }

    public void setUnreadMessages(int unreadMessages) {
        this.unreadMessages = unreadMessages;
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

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }
}
