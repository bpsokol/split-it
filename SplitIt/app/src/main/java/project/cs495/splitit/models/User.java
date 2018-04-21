package project.cs495.splitit.models;

import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

import project.cs495.splitit.Utils;


public class User implements EntityInterface {
    private String uid;
    private String name;
    private Map<String, Boolean> groups;
    private Map<String, Boolean> userReceipts;

    public User() {

    }

    public User(String uid, String name) {
        this.uid = uid;
        this.name = name;
    }

    public User(String uid, String name, Map<String, Boolean> groups, Map<String, Boolean> userReceipts) {
        this.uid = uid;
        this.name = name;
        this.groups = groups;
        this.userReceipts = userReceipts;
    }

    @Override
    public void commitToDB(DatabaseReference mDatabase) {
        mDatabase.child("users").child(this.uid).setValue(this);
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Boolean> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, Boolean> groups) {
        this.groups = groups;
    }

    public Map<String, Boolean> getUserReceipts() {
        return userReceipts;
    }

    public void setUserReceipts(Map<String, Boolean> userReceipts) {
        this.userReceipts = userReceipts;
    }

    public void addGroup(String groupId) {
        if (groups == null){
            groups = new HashMap<>();
        }
        this.groups.put(groupId, true);
        Utils.getDatabaseReference().child("users").child(uid).child("groups").child(groupId).setValue(true);
    }

    public void removeGroup(String groupId) {
        if (groups == null) {
            throw new NullPointerException("Trying to remove group from an empty list");
        } else {
            this.groups.remove(groupId);
        }
    }
}