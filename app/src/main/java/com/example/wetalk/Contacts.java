package com.example.wetalk;

public class Contacts {
    private String name;
    private String status;
    private String image;

    public Contacts()
    {
        this.setStatus(null);
        this.setName(null);
        this.setImage(null);
    }

    public Contacts(String name, String status, String image){
        this.setImage(image);
        this.setName(name);
        this.setStatus(status);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
