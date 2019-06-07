package com.example.saegeullee.applicationoneproject.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * 2018/12/28 1:52am
 *
 *     isLikePost 멤버의 타입을 boolean(소문자) 로 줬어야 되는데
 *     Boolean(대문자) 로 주니깐 계속 FeedListAdapter 에서 true false 검증하는 부분에서
 *     null pointer exception 이 떳다.
 *
 *     boolean VS Boolean???
 *     boolean 은 자료형이기 때문에 오직 true, false 만 들어갈 수 있고 null 은 못넣는다
 *     null 을 넣기위해선 바로 참조형인 Boolean 으로 적어야하는것이였다!!
 *     같은 맥락으로 int 에 null 을 못넣고 Integer 에 null 을 넣을수있는것처럼말이다
 *
 */

public class Post implements Parcelable {

    private String postId;
    private User user;
    private String description;
    private String date;
    private String postImage;
    private String likes;
    private boolean isLikePost;
    private List<Comment> commentList;
    private int commentNumber;

    public Post() {
    }


    protected Post(Parcel in) {
        postId = in.readString();
        user = in.readParcelable(User.class.getClassLoader());
        description = in.readString();
        date = in.readString();
        postImage = in.readString();
        likes = in.readString();
        isLikePost = in.readByte() != 0;
        commentNumber = in.readInt();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public int getCommentNumber() {
        return commentNumber;
    }

    public void setCommentNumber(int commentNumber) {
        this.commentNumber = commentNumber;
    }

    public boolean isLikePost() {
        return isLikePost;
    }

    public void setLikePost(boolean likePost) {
        isLikePost = likePost;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public List<Comment> getCommentList() {
        return commentList;
    }

    public void setCommentList(List<Comment> commentList) {
        this.commentList = commentList;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    @Override
    public String toString() {
        return "Post{" +
                "postId='" + postId + '\'' +
                ", user=" + user +
                ", description='" + description + '\'' +
                ", date='" + date + '\'' +
                ", postImage='" + postImage + '\'' +
                ", likes='" + likes + '\'' +
                ", isLikePost=" + isLikePost +
                ", commentList=" + commentList +
                ", commentNumber=" + commentNumber +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(postId);
        parcel.writeParcelable(user, i);
        parcel.writeString(description);
        parcel.writeString(date);
        parcel.writeString(postImage);
        parcel.writeString(likes);
        parcel.writeByte((byte) (isLikePost ? 1 : 0));
        parcel.writeInt(commentNumber);
    }
}
