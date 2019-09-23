package com.allianzcloud.chatfirebasedemo.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class UserVo implements Parcelable {

    String id , username, password;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.username);
        dest.writeString(this.password);
    }

    public UserVo() {
    }

    protected UserVo(Parcel in) {
        this.id = in.readString();
        this.username = in.readString();
        this.password = in.readString();
    }

    public static final Parcelable.Creator<UserVo> CREATOR = new Parcelable.Creator<UserVo>() {
        @Override
        public UserVo createFromParcel(Parcel source) {
            return new UserVo(source);
        }

        @Override
        public UserVo[] newArray(int size) {
            return new UserVo[size];
        }
    };
}
