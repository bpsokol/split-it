package project.cs495.splitit.models;

import com.google.firebase.database.DatabaseReference;

public interface Entity {
    void commitToDB(DatabaseReference mDatabase);
}
