package project.cs495.splitit.models;

import com.google.firebase.database.DatabaseReference;

import java.util.Map;

public class User implements EntityInterface {
    private String uid;
    private String name;
    private Map<String, Boolean> groups;
    private Map<String, Boolean> userReceipts;

    public User() {

    }

    User(String uid, String name, Map<String, Boolean> groups, Map<String, Boolean> userReceipts) {
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
}