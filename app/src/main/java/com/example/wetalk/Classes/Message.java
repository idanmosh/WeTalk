package com.example.wetalk.Classes;

import android.annotation.SuppressLint;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Message implements Serializable {

    private String date;
    private String from;
    private String message;
    private String type;
    private String send;
    private String messageId;
    private String state;
    private String image;
    private String link;

    public Message() { }

    public Message(String date, String from, String message, String type, String send, String messageId, String state, String image, String link) {
        this.message = message;
        this.type = type;
        this.from = from;
        this.date = date;
        this.send = send;
        this.messageId = messageId;
        this.state = state;
        this.image = image;
        this.link = link;
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

    @SuppressLint("SimpleDateFormat")
    public String getMessageTimeForContactView() {
        String DATE_FORMAT = "h:mm a dd MMM yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Date currDate = Calendar.getInstance().getTime();
        Date date = null;

        try {
            date = sdf.parse(this.date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date != null) {
            String[] splitCurr = currDate.toString().split(" ");
            String[] splitDate = date.toString().split(" ");
            if (checkIfToday(splitCurr, splitDate))
                return getMessageTime();
            else if (checkIfTomorrow(splitCurr, splitDate))
                return "אתמול";
            else {
                return splitDate[2] + "." + monthInNumber(splitDate[1]) + "." + splitDate[5];
            }
        }

        return getMessageTime();
    }

    private String monthInNumber(String month) {
        if (month.equals("Jan"))
            return "1";
        else if (month.equals("Feb"))
            return "2";
        else if (month.equals("Mar"))
            return "3";
        else if (month.equals("Apr"))
            return "4";
        else if (month.equals("May"))
            return "5";
        else if (month.equals("Jun"))
            return "6";
        else if (month.equals("Jul"))
            return "7";
        else if (month.equals("Aug"))
            return "8";
        else if (month.equals("Sep"))
            return "9";
        else if (month.equals("Oct"))
            return "10";
        else if (month.equals("Nov"))
            return "11";
        else
            return "12";
    }

    private boolean checkIfTomorrow(String[] splitCurr, String[] splitDate) {
        return (splitCurr[1].equals(splitDate[1])) &&
                (splitCurr[5].equals(splitDate[5])) &&
                ((Integer.parseInt(splitCurr[2]) - Integer.parseInt(splitDate[2])) == 1);
    }

    private boolean checkIfToday(String[] splitCurr, String[] splitDate) {
        return (splitCurr[0].equals(splitDate[0])) &&
                (splitCurr[1].equals(splitDate[1])) &&
                (splitCurr[2].equals(splitDate[2])) &&
                (splitCurr[5].equals(splitDate[5]));
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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

    public String getSend() {
        return send;
    }

    public void setSend(String send) {
        this.send = send;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
