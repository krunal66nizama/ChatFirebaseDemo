package com.allianzcloud.chatfirebasedemo.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MessagesVo {

    @SerializedName("msg")
    @Expose
    public String msg;
    @SerializedName("file")
    @Expose
    public String file;
    @SerializedName("thumb")
    @Expose
    public String thumb;
    @SerializedName("receiver")
    @Expose
    public String receiver;
    @SerializedName("sender")
    @Expose
    public String sender;
    @SerializedName("type")
    @Expose
    public String type;
    @SerializedName("timeStamp")
    @Expose
    public double timeStamp;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(double timeStamp) {
        this.timeStamp = timeStamp;
    }
}
