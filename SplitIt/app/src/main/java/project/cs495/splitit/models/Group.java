package project.cs495.splitit.models;

import com.google.firebase.database.DatabaseReference;

import java.util.List;
import java.util.ArrayList;

public class Group implements EntityInterface{
    private String groupId;
    private String groupName;
    private String managerUID;
    private String managerName;
    private List<String> members;
    private List<String> memberID;

    public Group() {
        this.members = new ArrayList<>();
        this.memberID = new ArrayList<>();
    }

    public Group(String groupId, String groupName, String managerUID, String managerName, List<String> member, List<String> memberID) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.managerUID = managerUID;
        this.managerName = managerName;
        if (member != null) {
            this.members = member;
        } else {
            this.members = new ArrayList<>();
        }
        if (memberID != null) {
            this.memberID = memberID;
        } else {
            this.memberID = new ArrayList<>();
        }
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getManagerUID() {
        return managerUID;
    }

    public void setManagerUID(String managerUID) {
        this.managerUID = managerUID;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public List<String> getMember() {
        return members;
    }

    public List<String> getMemberID() {
        return memberID;
    }

    public void setMemberID(List<String> memberID) {
        this.memberID = memberID;
    }

    public void setMember(List<String> member) {
        this.members = member;
    }

    public void addMember(String member, String memberID) {
        this.members.add(member);
        this.memberID.add(memberID);
    }

    public void removeMember(String member, String memberID) {
        this.members.remove(member);
        this.memberID.remove(memberID);
    }

    @Override
    public void commitToDB(DatabaseReference mDatabase) {
        mDatabase.child("groups").child(this.groupId).setValue(this);
    }
}