package com.applozic.mobicomkit.channel.service;

import java.util.ArrayList;
import java.util.List;

//ApplozicInternal: default
public class ApplozicAddMemberOfGroupType {
    List<String> groupMemberList = new ArrayList<>();
    String type;

    public List<String> getGroupMemberList() {
        return groupMemberList;
    }

    public void setGroupMemberList(List<String> groupMemberList) {
        this.groupMemberList = groupMemberList;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
