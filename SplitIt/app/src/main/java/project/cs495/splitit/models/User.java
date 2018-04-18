package project.cs495.splitit.models;

import java.util.UUID;

public class User {
    private UUID uid;
    private String lastName;
    private String firstName;

    public User() {

    }

    public User(UUID uid, String lName, String fName) {
        this.uid = uid;
        this.lastName = lName;
        this.firstName = fName;
    }

    public UUID getUid() {
        return uid;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

}
