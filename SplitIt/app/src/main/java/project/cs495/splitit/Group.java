package project.cs495.splitit;

import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class Group implements EntityInterface{
    private String groupId;
    private String groupName;
    private String managerUID;
    private String managerName;
    private Map<String,Boolean> members;

    public Group() {
        this.members = new HashMap<>();
    }

    public Group(String groupId, String groupName, String managerUID, String managerName, Map<String,Boolean> member) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.managerUID = managerUID;
        this.managerName = managerName;
        if (member != null) {
            this.members = member;
        } else {
            this.members = new HashMap<>();
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

    public Map<String, Boolean> getMember() {
        return members;
    }

    public void setMember(HashMap<String,Boolean> member) {
        this.members = member;
    }

    public void addMember(String member) {
        this.members.put(member,true);
    }

    public void removeMember(String member) {
        this.members.remove(member);
    }

    @Override
    public void commitToDB(DatabaseReference mDatabase) {
        mDatabase.child("receipts").child(this.groupId).setValue(this);
    }
}
