package project.cs495.splitit.models;

import java.util.HashMap;
import java.util.Map;

import project.cs495.splitit.Utils;

public class GroupOwner extends User {

    private Map<String, Boolean> groupsOwned;

    public GroupOwner(String managerUID, String managerName) {
        super(managerUID, managerName);
    }

    public Map<String, Boolean> getGroupsOwned() {
        return groupsOwned;
    }

    public void setGroupsOwned(Map<String, Boolean> groupsOwned) {
        this.groupsOwned = groupsOwned;
    }

    public void addGroupOwned(String groupId) {
        if (this.groupsOwned == null) {
            this.groupsOwned = new HashMap<>();
        }
        this.groupsOwned.put(groupId, true);
        Utils.getDatabaseReference().child("users").child(this.getUid())
                .child("groupsOwned").child(groupId).setValue(true);
    }
}
