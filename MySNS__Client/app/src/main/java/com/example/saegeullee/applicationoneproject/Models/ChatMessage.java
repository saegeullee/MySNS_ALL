package com.example.saegeullee.applicationoneproject.Models;

public class ChatMessage {

    private String profile_image;
    private String message;
    private String date;
    private String user_name;
    //사용자 id
    private String id;
    private String user_id;
    private String msg_type;

    public ChatMessage() {
    }

    public ChatMessage(String profile_image, String message, String date, String user_name, String id, String user_id, String msg_type) {
        this.profile_image = profile_image;
        this.message = message;
        this.date = date;
        this.user_name = user_name;
        this.id = id;
        this.user_id = user_id;
        this.msg_type = msg_type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(String msg_type) {
        this.msg_type = msg_type;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "profile_image='" + profile_image + '\'' +
                ", message='" + message + '\'' +
                ", date='" + date + '\'' +
                ", user_name='" + user_name + '\'' +
                ", id='" + id + '\'' +
                ", user_id='" + user_id + '\'' +
                ", msg_type='" + msg_type + '\'' +
                '}';
    }
}
