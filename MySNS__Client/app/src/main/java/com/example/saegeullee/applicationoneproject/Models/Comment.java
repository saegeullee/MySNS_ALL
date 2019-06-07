package com.example.saegeullee.applicationoneproject.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Comment implements Parcelable {

    private String commentId;
    private String postId;
    private String commentText;
    private User user;
    private String date;

    public Comment() {
    }

    public Comment(String commentId, String postId, String commentText, User user, String date) {
        this.commentId = commentId;
        this.postId = postId;
        this.commentText = commentText;
        this.user = user;
        this.date = date;
    }

    protected Comment(Parcel in) {
        commentId = in.readString();
        postId = in.readString();
        commentText = in.readString();
        user = in.readParcelable(User.class.getClassLoader());
        date = in.readString();
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "commentId='" + commentId + '\'' +
                ", postId='" + postId + '\'' +
                ", commentText='" + commentText + '\'' +
                ", user=" + user +
                ", date='" + date + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(commentId);
        parcel.writeString(postId);
        parcel.writeString(commentText);
        parcel.writeParcelable(user, i);
        parcel.writeString(date);
    }
}
