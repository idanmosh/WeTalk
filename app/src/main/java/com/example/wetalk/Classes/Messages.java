package com.example.wetalk.Classes;

public class Messages {

    private String date, from, message, type, messageId;

    public Messages() { }

    public Messages(String date, String from, String message, String type, String messageId) {
        this.message = message;
        this.type = type;
        this.from = from;
        this.date = date;
        this.messageId = messageId;
    }

    public String getMessageTime() {
        String[] str = date.split(" ");
        String AM_or_PM = str[1];
        if (AM_or_PM.equals("AM"))
            return str[0];
        str = str[0].split(":");
        if (AM_or_PM.equals("PM")) {
            if ((Integer.parseInt(str[0]) >= 1) && (Integer.parseInt(str[0]) <= 11)) {
                str[0] = String.valueOf(Integer.parseInt(str[0]) + 12);
            }
        }

        return str[0] + ":" + str[1];
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
