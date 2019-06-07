package com.example.saegeullee.applicationoneproject.Models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.example.saegeullee.applicationoneproject.R;

import org.json.JSONException;
import org.json.JSONObject;

public class User implements Parcelable {

    private static final String TAG = "User";

    private int id;
    private String user_id;
    private String user_name;
    private String user_email;
    private String profile_image;
    private Boolean is_following;
    private Boolean is_follower;
    private boolean is_friend;

    private boolean isRadioBtnChecked;
    private boolean isInChatRoom;

    public User() {
    }

    public User(JSONObject jsonObject, Context context) {

        try {

            this.id = jsonObject.getInt(context.getString(R.string.db_users_id));
            this.user_id = jsonObject.getString(context.getString(R.string.db_users_user_id));
            this.user_name = jsonObject.getString(context.getString(R.string.db_users_user_name));
            this.user_email = jsonObject.getString(context.getString(R.string.db_users_user_email));

            if(jsonObject.has(context.getString(R.string.db_users_user_profile_image))) {

                this.profile_image = jsonObject.getString(context.getString(R.string.db_users_user_profile_image));
            }

            if(jsonObject.has("isInChatRoom")) {
                this.isInChatRoom = jsonObject.getBoolean("isInChatRoom");
            }

        } catch (JSONException e) {
            e.printStackTrace();

        } finally {
            Log.d(TAG, "User: " + jsonObject.toString());
        }
    }


    protected User(Parcel in) {
        id = in.readInt();
        user_id = in.readString();
        user_name = in.readString();
        user_email = in.readString();
        profile_image = in.readString();
        byte tmpIs_following = in.readByte();
        is_following = tmpIs_following == 0 ? null : tmpIs_following == 1;
        byte tmpIs_follower = in.readByte();
        is_follower = tmpIs_follower == 0 ? null : tmpIs_follower == 1;
        is_friend = in.readByte() != 0;
        isRadioBtnChecked = in.readByte() != 0;
        isInChatRoom = in.readByte() != 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public boolean isInChatRoom() {
        return isInChatRoom;
    }

    public void setInChatRoom(boolean inChatRoom) {
        isInChatRoom = inChatRoom;
    }

    public boolean isRadioBtnChecked() {
        return isRadioBtnChecked;
    }

    public void setRadioBtnChecked(boolean radioBtnChecked) {
        isRadioBtnChecked = radioBtnChecked;
    }

    public boolean isIs_friend() {
        return is_friend;
    }

    public void setIs_friend(boolean is_friend) {
        this.is_friend = is_friend;
    }

    public Boolean getIs_following() {
        return is_following;
    }

    public void setIs_following(Boolean is_following) {
        this.is_following = is_following;
    }

    public Boolean getIs_follower() {
        return is_follower;
    }

    public void setIs_follower(Boolean is_follower) {
        this.is_follower = is_follower;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }


    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {

        this.profile_image = profile_image;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", user_id='" + user_id + '\'' +
                ", isInChatRoom : " + isInChatRoom +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(user_id);
        dest.writeString(user_name);
        dest.writeString(user_email);
        dest.writeString(profile_image);
        dest.writeByte((byte) (is_following == null ? 0 : is_following ? 1 : 2));
        dest.writeByte((byte) (is_follower == null ? 0 : is_follower ? 1 : 2));
        dest.writeByte((byte) (is_friend ? 1 : 0));
        dest.writeByte((byte) (isRadioBtnChecked ? 1 : 0));
        dest.writeByte((byte) (isInChatRoom ? 1 : 0));
    }

//    @Override
//    public String toString() {
//        return "User{" +
//                "id=" + id +
//                ", user_id='" + user_id + '\'' +
//                ", user_name='" + user_name + '\'' +
//                ", user_email='" + user_email + '\'' +
//                ", profile_image='" + profile_image + '\'' +
//                ", is_following=" + is_following +
//                ", is_follower=" + is_follower +
//                ", is_friend=" + is_friend +
//                '}';
//    }

}
