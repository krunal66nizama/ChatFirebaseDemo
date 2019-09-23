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
    @SerializedName("receiver")
    @Expose
    public String receiver;
    @SerializedName("sender")
    @Expose
    public String sender;
    @SerializedName("type")
    @Expose
    public String type;

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
}
