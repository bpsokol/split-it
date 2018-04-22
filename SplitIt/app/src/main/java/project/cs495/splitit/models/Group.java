package project.cs495.splitit.models;

import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class Group implements EntityInterface{
    private String groupId;
    private String groupName;
    private String managerUID;
    private String managerName;
    private String email;
    private Map<String, Boolean> members;
    private Map<String, Boolean> memberID;

    public Group() {
        this.members = new HashMap<>();
        this.memberID = new HashMap<>();
    }

    public Group(String groupId, String groupName, String managerUID, String managerName, String email, Map<String, Boolean> member, Map<String, Boolean> memberID) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.managerUID = managerUID;
        this.managerName = managerName;
        this.email = email;
        if (member != null) {
            this.members = member;
        } else {
            this.members = new HashMap<>();
        }
        if (memberID != null) {
            this.memberID = memberID;
        } else {
            this.memberID = new HashMap<>();
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Map<String, Boolean> getMembers() {
        return members;
    }

    public Map<String, Boolean> getMemberID() {
        return memberID;
    }

    public void setMemberID(Map<String, Boolean> memberID) {
        this.memberID = memberID;
    }

    public void setMembers(Map<String, Boolean> member) {
        this.members = member;
    }

    public void addMember(String member, String memberID) {
        this.members.put(member, true);
        this.memberID.put(memberID, true);
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