package project.cs495.splitit.models;

import com.google.firebase.database.DatabaseReference;

public interface EntityInterface {
    void commitToDB(DatabaseReference mDatabase);
}
