package com.restkeeper.utils;

public enum MessageType {
    ENTERPRISE_ACCOUNT_CREATION(0, "Account Creation"),
    ENTERPRISE_ACCOUNT_PASSWORD_RESET(1, "Password Reset"),
    ENTERPRISE_ACCOUNT_INFORMATION_UPDATE(2,"Account Information Update");

    private  int type;
    private  String  desc;

    MessageType(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}