package project.cs495.splitit.models;

import java.util.Map;

public class UserBuilder {
    private String uid;
    private String name;
    private String email;
    private Map<String, Boolean> groups;
    private Map<String, Boolean> userReceipts;

    public UserBuilder setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public UserBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public UserBuilder setEmail(String email) {
        this.email = email;
        return this;
    }

    public UserBuilder setGroups(Map<String, Boolean> groups) {
        this.groups = groups;
        return this;
    }

    public UserBuilder setUserReceipts(Map<String, Boolean> userReceipts) {
        this.userReceipts = userReceipts;
        return this;
    }

    public User createUser() {
        return new User(uid, name, email, groups, userReceipts);
    }
}